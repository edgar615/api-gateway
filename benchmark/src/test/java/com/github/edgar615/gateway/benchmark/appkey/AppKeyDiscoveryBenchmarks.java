//package com.github.edgar615.dirwolves.benchmark.appkey;
//
//import com.github.edgar615.gateway.plugin.appkey.discovery.AppKey;
//import com.github.edgar615.gateway.plugin.appkey.discovery.AppKeyDiscovery;
//import io.vertx.core.AsyncResult;
//import io.vertx.core.Handler;
//import io.vertx.core.Vertx;
//import io.vertx.core.json.JsonObject;
//import org.openjdk.jmh.annotations.*;
//
//import java.util.UUID;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by edgar on 17-7-25.
// */
//@State(Scope.Benchmark)
//public class AppKeyDiscoveryBenchmarks {
//
//  @State(Scope.Benchmark)
//  public static class AppKeyBackend {
//    private Vertx vertx;
//
//    private AppKeyDiscovery discovery;
//
//    public AppKeyBackend() {
//      vertx = Vertx.vertx();
//      discovery = AppKeyDiscovery.create(vertx, "api");
//      for (int i = 0; i < 1000; i ++) {
//        String appKey = "appkey_" + i;
//        JsonObject jsonObject = new JsonObject()
//            .put("appKey", appKey)
//            .put("appSecret", UUID.randomUUID().toString());
//        discovery.publish(new AppKey(appKey, jsonObject), ar -> {
//
//        });
//      }
//      try {
//        TimeUnit.SECONDS.sleep(3);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//    }
//
//    public void getAppKey(String appkey,
//                               Handler<AsyncResult<AppKey>>
//                                   handler) {
//      discovery.getAppKey(appkey, handler);
//    }
//
//    public void close() {
//      vertx.close();
//    }
//  }
//
//  @TearDown(Level.Trial)
//  public void tearDown(AppKeyBackend pool) {
//    pool.close();
//  }
//
//  @Benchmark
//  @BenchmarkMode(Mode.Throughput)
//  @OutputTimeUnit(TimeUnit.MILLISECONDS)
//  @Fork(1)
//  @OperationsPerInvocation(10000)
//  public void testApi(AppKeyBackend pool) {
//    final CountDownLatch latch = new CountDownLatch(1);
//    pool.getAppKey("appkey_6", ar -> {
//      latch.countDown();
//    });
//    try {
//      latch.await();
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//  }
//
//  @Benchmark
//  @BenchmarkMode(Mode.AverageTime)
//  @OutputTimeUnit(TimeUnit.NANOSECONDS)
//  @Fork(1)
//  @OperationsPerInvocation(10000)
//  public void testAverage(AppKeyBackend backend) {
//    final CountDownLatch latch = new CountDownLatch(1);
//    backend.getAppKey("appkey_6", ar -> {
//      latch.countDown();
//    });
//    try {
//      latch.await();
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//  }
//
//}
