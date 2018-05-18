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
      <version>0.0.9</version>
</dependency>
<dependency>
    <groupId>com.uber.jaeger</groupId>
    <artifactId>jaeger-core</artifactId>
    <version>0.26.0</version>
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

## Reference
[simple-opentracing-demo](https://github.com/brucewu-fly/simple-opentracing-demo)

[spring-boot-opentracing-demo](https://github.com/brucewu-fly/spring-boot-opentracing-demo)
