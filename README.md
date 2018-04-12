# Aliyun LOG Jaeger Sender
## Introduction
These components make Jaeger compatible with [Aliyun Log Service](https://help.aliyun.com/product/28958.html).

## Usage
###  Adding the Dependencies in pom.xml
```
<dependency>
      <groupId>io.opentracing</groupId>
      <artifactId>opentracing-api</artifactId>
      <version>0.31.0</version>
</dependency>
<dependency>
      <groupId>com.aliyun.openservices</groupId>
      <artifactId>aliyun-log-jaeger-sender</artifactId>
      <version>0.0.2</version>
</dependency>
```

### Build a Tracer
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
