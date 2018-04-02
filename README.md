# Aliyun LOG Jaeger Sender
## Introduction
These components make Jaeger compatible with [Aliyun Log Service](https://help.aliyun.com/product/28958.html).

## Usage
```java
aliyunLogSender = new AliyunLogSender
      .Builder(projectName, logStore, endpoint, accessKeyId, accessKey)
      .withTopic(topic)
      .build();
RemoteReporter remoteReporter = new RemoteReporter.Builder().withSender(aliyunLogSender).build();
tracer = new Tracer.Builder(serviceName)
  .withReporter(remoteReporter)
  .withSampler(new ConstSampler(true))
  .build();
```
