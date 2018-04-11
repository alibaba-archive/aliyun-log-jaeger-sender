package com.aliyun.openservices.log.jaeger.sender;

import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.producer.LogProducer;
import com.aliyun.openservices.log.producer.ProducerConfig;
import com.aliyun.openservices.log.producer.ProjectConfig;
import com.uber.jaeger.Span;
import com.uber.jaeger.exceptions.SenderException;
import com.uber.jaeger.senders.Sender;

import java.util.ArrayList;
import java.util.List;

public class AliyunLogSender implements Sender {

  private LogProducer producer;

  private Builder builder;

  public AliyunLogSender(String projectName, String logStore, String endpoint, String accessKeyId,
      String accessKeySecret) {
    this(new Builder(projectName, logStore, endpoint, accessKeyId, accessKeySecret));
  }

  private AliyunLogSender(Builder builder) {
    this.builder = builder;
    producer = new LogProducer(builder.producerConfig);
    producer.setProjectConfig(builder.projectConfig);
  }

  @Override
  public int append(Span span) throws SenderException {
    LogItem logItem = LogItemSpanConverter.convertSpan(span);
    List<LogItem> logItems = new ArrayList<LogItem>();
    logItems.add(logItem);
    producer.send(builder.projectConfig.projectName, builder.logStore, builder.topic, "", logItems,
        new AliyunLogSenderCallback(builder.projectConfig.projectName, builder.logStore,
            builder.topic, logItems));
    return 0;
  }

  @Override
  public int flush() throws SenderException {
    producer.flush();
    return 0;
  }

  @Override
  public int close() throws SenderException {
    producer.flush();
    producer.close();
    return 0;
  }

  public static class Builder {

    private String logStore;

    private String topic = "";

    private ProjectConfig projectConfig = new ProjectConfig();

    private ProducerConfig producerConfig = new ProducerConfig();

    public Builder(String projectName, String logStore, String endpoint, String accessKeyId,
        String accessKeySecret) {
      projectConfig.projectName = projectName;
      projectConfig.endpoint = endpoint;
      projectConfig.accessKeyId = accessKeyId;
      projectConfig.accessKey = accessKeySecret;
      this.logStore = logStore;
    }

    public Builder withPackageTimeoutInMS(int packageTimeoutInMS) {
      producerConfig.packageTimeoutInMS = packageTimeoutInMS;
      return this;
    }

    public Builder withLogsCountPerPackage(int logsCountPerPackage) {
      producerConfig.logsCountPerPackage = logsCountPerPackage;
      return this;
    }

    public Builder withLogsBytesPerPackage(int logsBytesPerPackage) {
      producerConfig.logsBytesPerPackage = logsBytesPerPackage;
      return this;
    }

    public Builder withMemPoolSizeInByte(int memPoolSizeInByte) {
      producerConfig.memPoolSizeInByte = memPoolSizeInByte;
      return this;
    }

    public Builder withMaxIOThreadSizeInPool(int maxIOThreadSizeInPool) {
      producerConfig.maxIOThreadSizeInPool = maxIOThreadSizeInPool;
      return this;
    }

    public Builder withRetryTimes(int retryTimes) {
      producerConfig.retryTimes = retryTimes;
      return this;
    }

    public Builder withTopic(String topic) {
      this.topic = topic;
      return this;
    }

    public AliyunLogSender build() {
      return new AliyunLogSender(this);
    }
  }
}
