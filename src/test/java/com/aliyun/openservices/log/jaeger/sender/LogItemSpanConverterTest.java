package com.aliyun.openservices.log.jaeger.sender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.log.common.LogContent;
import com.aliyun.openservices.log.common.LogItem;
import com.uber.jaeger.Tracer;
import com.uber.jaeger.reporters.InMemoryReporter;
import com.uber.jaeger.samplers.ConstSampler;
import io.opentracing.Span;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class LogItemSpanConverterTest {

  private Tracer tracer;

  @Before
  public void setUp() {
    tracer = new Tracer.Builder("test-service-name")
        .withReporter(new InMemoryReporter())
        .withSampler(new ConstSampler(true))
        .build();
  }

  @Test
  public void testConvertSpan() {
    Map<String, Object> fields = new HashMap<String, Object>();
    fields.put("k", "v");

    Span span = tracer.buildSpan("operation-name").start();
    span = span.log(1, fields);
    span = span.setBaggageItem("foo", "bar");
    span = span.setTag("num", 10);
    span = span.setTag("str", "abc");
    span = span.setTag("bool", true);

    LogItem logItem = LogItemSpanConverter.convertSpan((com.uber.jaeger.Span) span);
    Map<String, String> logContentsMap = convertLogContents(logItem.GetLogContents());
    assertEquals("operation-name", logContentsMap.get(Constants.OPERATION_NAME));

    // check tags
    JSONObject tags = (JSONObject) JSONObject.parse(logContentsMap.get(Constants.TAGS));
    assertTrue(tags.containsKey("num"));
    assertTrue(tags.containsValue(10));
    assertTrue(tags.containsKey("str"));
    assertTrue(tags.containsValue("abc"));
    assertTrue(tags.containsKey("bool"));
    assertTrue(tags.containsValue(true));

    // check logs
    JSONArray logs = JSONArray.parseArray(logContentsMap.get(Constants.LOGS));
    assertEquals(2, logs.size());
    JSONObject log = (JSONObject) logs.get(0);
    assertEquals(1, log.get(Constants.TIMESTAMP));
    tags = (JSONObject) log.get(Constants.TAGS);
    assertEquals(1, tags.size());
    assertTrue(tags.containsKey("k"));
    assertTrue(tags.containsValue("v"));

    log = (JSONObject) logs.get(1);
    tags = (JSONObject) log.get(Constants.TAGS);
    assertEquals(3, tags.size());
    assertTrue(tags.containsKey("event"));
    assertTrue(tags.containsValue("baggage"));
    assertTrue(tags.containsKey("key"));
    assertTrue(tags.containsValue("foo"));
    assertTrue(tags.containsKey("value"));
    assertTrue(tags.containsValue("bar"));

    // check process
    JSONObject process = JSONObject.parseObject(logContentsMap.get(Constants.PROCESS));
    assertEquals("test-service-name", process.get(Constants.SERVICE_NAME));
  }

  @Test
  public void testBuildTags() {
    Map<String, Object> tags = new HashMap<String, Object>();
    tags.put("key", "value");

    JSONObject jsonTags = LogItemSpanConverter.buildTags(tags);
    assertNotNull(jsonTags);
    assertEquals(1, jsonTags.size());
    assertTrue(tags.containsKey("key"));
    assertTrue(tags.containsValue("value"));

    jsonTags = LogItemSpanConverter.buildTags(null);
    assertNotNull(jsonTags);
    assertEquals(0, jsonTags.size());
  }

  private Map<String, String> convertLogContents(ArrayList<LogContent> logContents) {
    Map<String, String> logContentsMap = new HashMap<String, String>();
    for (LogContent logContent : logContents) {
      logContentsMap.put(logContent.mKey, logContent.mValue);
    }
    return logContentsMap;
  }

}
