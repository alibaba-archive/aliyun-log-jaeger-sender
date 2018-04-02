package com.aliyun.openservices.log.jaeger.sender;

import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.producer.ILogCallback;
import com.aliyun.openservices.log.response.PutLogsResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AliyunLogSenderCallback extends ILogCallback {

  private static final Logger LOGGER = LoggerFactory.getLogger(AliyunLogSenderCallback.class);

  private String project;

  private String logStore;

  private String topic;

  private List<LogItem> logItems;

  public AliyunLogSenderCallback(String project, String logStore, String topic,
      List<LogItem> logItems) {
    super();
    this.project = project;
    this.logStore = logStore;
    this.topic = topic;
    this.logItems = logItems;
  }

  @Override
  public void onCompletion(PutLogsResponse putLogsResponse, LogException e) {
    if (e != null) {
      LOGGER.error("Failed to putLogs. project=" + project
          + " logStore=" + logStore + " topic=" + topic + " logItems=" + logItems, e);
    }
  }

}
