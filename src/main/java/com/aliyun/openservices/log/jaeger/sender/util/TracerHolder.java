package com.aliyun.openservices.log.jaeger.sender.util;

import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import java.io.Closeable;

public class TracerHolder implements Tracer, Closeable {

  private static final TracerHolder INSTANCE = new TracerHolder();

  private static volatile com.uber.jaeger.Tracer tracer;

  public static TracerHolder get() {
    return INSTANCE;
  }

  public static synchronized void register(final com.uber.jaeger.Tracer tracer) {
    if (tracer == null) {
      throw new NullPointerException("Cannot register TracerHolder <null>.");
    }
    if (isRegistered() && !TracerHolder.tracer.equals(tracer)) {
      throw new IllegalStateException("There is already a current Tracer registered.");
    }
    TracerHolder.tracer = tracer;
  }

  public static synchronized boolean isRegistered() {
    return TracerHolder.tracer != null;
  }

  @Override
  public ScopeManager scopeManager() {
    return tracer.scopeManager();
  }

  @Override
  public SpanBuilder buildSpan(String operationName) {
    return tracer.buildSpan(operationName);
  }

  @Override
  public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
    tracer.inject(spanContext, format, carrier);
  }

  @Override
  public <C> SpanContext extract(Format<C> format, C carrier) {
    return tracer.extract(format, carrier);
  }

  @Override
  public Span activeSpan() {
    return tracer.activeSpan();
  }

  @Override
  public void close() {
    tracer.close();
  }
}
