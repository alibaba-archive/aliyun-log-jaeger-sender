package com.aliyun.openservices.log.jaeger.sender;

import com.aliyun.openservices.log.jaeger.sender.util.ThrowableTransformer;
import org.junit.Test;

public class ThrowableTransformerTest {

  @Test
  public void testConvert2String() {
    try {
      f1();
    } catch (Exception e) {
      System.out.println(ThrowableTransformer.INSTANCE.convert2String(e));
    }
  }

  private static void f1() {
    f2();
  }

  private static void f2() {
    f3();
  }

  private static void f3() {
    f4();
  }

  private static void f4() {
    int a = 10 / 0;
  }

}
