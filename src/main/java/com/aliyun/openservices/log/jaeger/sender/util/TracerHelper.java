package com.aliyun.openservices.log.jaeger.sender.util;

import com.aliyun.openservices.log.jaeger.sender.AliyunLogSender;
import com.uber.jaeger.reporters.RemoteReporter;
import com.uber.jaeger.samplers.Sampler;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.propagation.Format;
import java.util.Map;

public class TracerHelper {

  private static volatile com.uber.jaeger.Tracer tracer;

  public static synchronized void buildTracer(String serviceName, AliyunLogSender aliyunLogSender,
      Sampler sampler) {
    RemoteReporter remoteReporter = new RemoteReporter.Builder()
        .withSender(aliyunLogSender)
        .build();
    tracer = new com.uber.jaeger.Tracer.Builder(serviceName)
        .withReporter(remoteReporter)
        .withSampler(sampler)
        .build();
  }

  public static synchronized void registerTracer(final com.uber.jaeger.Tracer tracer) {
    if (tracer == null) {
      throw new NullPointerException("Cannot register Tracer <null>.");
    }
    if (isTracerRegistered() && !TracerHelper.tracer.equals(tracer)) {
      throw new IllegalStateException("There is already a current Tracer registered.");
    }
    TracerHelper.tracer = tracer;
  }

  public static synchronized boolean isTracerRegistered() {
    return TracerHelper.tracer != null;
  }

  public static Scope traceLatency(String operationName, boolean finishSpanOnClose) {
    return TracerHelper.buildSpan(operationName).startActive(finishSpanOnClose);
  }

  public static Scope traceLatency(String operationName, boolean finishSpanOnClose,
      String spanContextString) {
    SpanContext context = com.uber.jaeger.SpanContext.contextFromString(spanContextString);
    Tracer.SpanBuilder spanBuilder = TracerHelper.buildSpan(operationName).asChildOf(context);
    return spanBuilder.startActive(finishSpanOnClose);
  }

  public static Scope traceLatency(String operationName, boolean finishSpanOnClose,
      String spanContextString, Map<String, String> baggage) {
    SpanContext context = com.uber.jaeger.SpanContext.contextFromString(spanContextString);
    ((com.uber.jaeger.SpanContext) context).withBaggage(baggage);
    Tracer.SpanBuilder spanBuilder = TracerHelper.buildSpan(operationName).asChildOf(context);
    return spanBuilder.startActive(finishSpanOnClose);
  }

  public static Scope asyncTraceLatency(Scope scope, boolean finishSpanOnClose) {
    return TracerHelper.scopeManager().activate(scope.span(), finishSpanOnClose);
  }

  public static ScopeManager scopeManager() {
    return tracer.scopeManager();
  }

  public static SpanBuilder buildSpan(String operationName) {
    return tracer.buildSpan(operationName);
  }

  public static <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
    tracer.inject(spanContext, format, carrier);
  }

  public static <C> SpanContext extract(Format<C> format, C carrier) {
    return tracer.extract(format, carrier);
  }

  public static String getActiveSpanContextString() {
    com.uber.jaeger.SpanContext spanContext = (com.uber.jaeger.SpanContext) TracerHelper
        .activeSpan().context();
    return spanContext.contextAsString();
  }

  public static Iterable<Map.Entry<String, String>> getActiveSpanBaggageItems() {
    return TracerHelper.activeSpan().context().baggageItems();
  }

  public static Span activeSpan() {
    return tracer.activeSpan();
  }

  public static void closeTracer() {
    tracer.close();
  }

}
