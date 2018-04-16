package com.aliyun.openservices.log.jaeger.sender.util;

import com.aliyun.openservices.log.jaeger.sender.AliyunLogSender;
import com.uber.jaeger.reporters.RemoteReporter;
import com.uber.jaeger.samplers.Sampler;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.propagation.Format;

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

  public static Span activeSpan() {
    return tracer.activeSpan();
  }

  public static void closeTracer() {
    tracer.close();
  }

}
