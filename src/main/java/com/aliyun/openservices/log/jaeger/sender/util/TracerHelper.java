package com.aliyun.openservices.log.jaeger.sender.util;

import com.aliyun.openservices.log.jaeger.sender.AliyunLogSender;
import com.aliyun.openservices.log.jaeger.sender.AliyunLogSenderCallback;
import com.uber.jaeger.reporters.NoopReporter;
import com.uber.jaeger.reporters.RemoteReporter;
import com.uber.jaeger.reporters.Reporter;
import com.uber.jaeger.samplers.Sampler;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.propagation.Format;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TracerHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(AliyunLogSenderCallback.class);

  private static volatile com.uber.jaeger.Tracer tracer;

  public static synchronized void buildTracer(String serviceName, AliyunLogSender aliyunLogSender,
      Sampler sampler) {
    if (aliyunLogSender == null) {
      LOGGER.warn(
          "The parameter aliyunLogSender is null, use NoopReporter instead of RemoteReporter.");
      buildTracer(serviceName, new NoopReporter(), sampler);
    } else {
      RemoteReporter remoteReporter = new RemoteReporter.Builder()
          .withSender(aliyunLogSender)
          .build();
      buildTracer(serviceName, remoteReporter, sampler);
    }
  }

  public static synchronized void buildTracer(String serviceName, Reporter reporter,
      Sampler sampler) {
    if (tracer == null) {
      tracer = new com.uber.jaeger.Tracer.Builder(serviceName)
          .withReporter(reporter)
          .withSampler(sampler)
          .build();
    }
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

  public static synchronized void updateTracer(final com.uber.jaeger.Tracer tracer) {
    if (tracer == null) {
      throw new NullPointerException("Cannot register Tracer <null>.");
    }
    TracerHelper.tracer = tracer;
  }

  public static synchronized boolean isTracerRegistered() {
    return TracerHelper.tracer != null;
  }

  public static Scope traceLatency(String operationName) {
    return TracerHelper.buildSpan(operationName).startActive(true);
  }

  public static Scope traceLatency(String operationName, String spanContextString) {
    SpanContext context = com.uber.jaeger.SpanContext.contextFromString(spanContextString);
    Tracer.SpanBuilder spanBuilder = TracerHelper.buildSpan(operationName).asChildOf(context);
    return spanBuilder.startActive(true);
  }

  public static Scope traceLatency(String operationName, String spanContextString,
      Map<String, String> baggage) {
    SpanContext context = com.uber.jaeger.SpanContext.contextFromString(spanContextString);
    ((com.uber.jaeger.SpanContext) context).withBaggage(baggage);
    Tracer.SpanBuilder spanBuilder = TracerHelper.buildSpan(operationName).asChildOf(context);
    return spanBuilder.startActive(true);
  }

  public static Scope asyncTraceLatency(String operationName) {
    return TracerHelper.buildSpan(operationName).startActive(false);
  }

  public static Scope asyncTraceLatency(String operationName, String spanContextString) {
    SpanContext context = com.uber.jaeger.SpanContext.contextFromString(spanContextString);
    Tracer.SpanBuilder spanBuilder = TracerHelper.buildSpan(operationName).asChildOf(context);
    return spanBuilder.startActive(false);
  }

  public static Scope asyncTraceLatency(String operationName, String spanContextString,
      Map<String, String> baggage) {
    SpanContext context = com.uber.jaeger.SpanContext.contextFromString(spanContextString);
    ((com.uber.jaeger.SpanContext) context).withBaggage(baggage);
    Tracer.SpanBuilder spanBuilder = TracerHelper.buildSpan(operationName).asChildOf(context);
    return spanBuilder.startActive(false);
  }

  public static Scope restoreAsyncTraceLatency(Scope scope) {
    return TracerHelper.scopeManager().activate(scope.span(), true);
  }

  public static void logThrowable(Span span, Throwable t) {
    Map<String, String> fields = new HashMap<String, String>();
    fields.put("event", "error");
    fields.put("error.kind", t.getClass().getName());
    fields.put("message", t.getMessage());
    fields.put("stack", ThrowableTransformer.INSTANCE.convert2String(t));
    span.log(fields);
  }

  public static void logThrowable(Span span, long timestampMicroseconds, Throwable t) {
    Map<String, String> fields = new HashMap<String, String>();
    fields.put("event", "error");
    fields.put("error.kind", t.getClass().getName());
    fields.put("message", t.getMessage());
    fields.put("stack", ThrowableTransformer.INSTANCE.convert2String(t));
    span.log(fields);
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
