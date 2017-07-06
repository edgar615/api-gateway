//package com.edgar.service.discovery;
//
//import ServiceDiscoveryVerticle;
//import io.vertx.core.AbstractVerticle;
//import io.vertx.core.Vertx;
//import io.vertx.core.VertxOptions;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.unit.TestContext;
//import io.vertx.ext.unit.junit.VertxUnitRunner;
//import io.vertx.servicediscovery.Record;
//import io.vertx.servicediscovery.ServiceDiscovery;
//import io.vertx.servicediscovery.types.HttpEndpoint;
//import org.awaitility.Awaitility;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
///**
// * Created by Edgar on 2017/5/10.
// *
// * @author Edgar  Date 2017/5/10
// */
//@RunWith(VertxUnitRunner.class)
//public class ServiceEventbusClusterTest {
//
//  private Vertx vertx;
//
//  private MoreServiceDiscovery moreServiceDiscovery;
//
//  private ServiceDiscovery discovery;
//
//  @Before
//  public void setUp(TestContext testContext) {
//    AtomicBoolean complete = new AtomicBoolean();
//    Vertx.clusteredVertx(new VertxOptions().setClustered(true), ar-> {
//      vertx = ar.result();
//      complete.set(true);
//    });
//    Awaitility.await().until(() -> complete.get());
//    discovery = ServiceDiscovery.create(vertx);
//
//    AtomicBoolean complete2 = new AtomicBoolean();
//    vertx.deployVerticle(ServiceDiscoveryVerticle.class.getName(), ar -> {
//      complete2.set(true);
//    });
//    Awaitility.await().until(() -> complete2.get());
//  }
//
//  @After
//  public void tearDwon() {
//    AtomicBoolean complete = new AtomicBoolean();
//    vertx.close(ar -> {
//      complete.set(true);
//    });
//    Awaitility.await().until(() -> complete.get());
//  }
//
//  @Test
//  public void testQueryForNames(TestContext testContext) {
//    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions());
//
//    AtomicInteger seq = new AtomicInteger();
//    List<Record> records = new ArrayList<>();
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8081, "/"));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8082, "/"));
//    records.add(HttpEndpoint.createRecord("test2", "localhost", 8083, "/"));
//
//    ClusterDiscovery clusterDiscovery = new ClusterDiscovery(records, seq);
//    Awaitility.await().until(() -> seq.get() == records.size());
//
//    AtomicBoolean complete = new AtomicBoolean();
//    vertx.eventBus().<JsonObject>send("service.discovery.queryForNames", new JsonObject(), ar -> {
//      if (ar.failed()) {
//        ar.cause().printStackTrace();
//        return;
//      }
//      System.out.println(ar.result().body());
//      testContext.assertEquals(2, ar.result().body().size());
//      testContext.assertEquals(2, ar.result().body().getJsonObject("test").getInteger("instances"));
//      testContext.assertEquals(1, ar.result().body().getJsonObject("test2").getInteger
//              ("instances"));
//      complete.set(true);
//    });
//
//    Awaitility.await().until(() -> complete.get());
//
//    AtomicInteger selectSeq = new AtomicInteger();
//    List<Integer> selected = new CopyOnWriteArrayList<>();
//    List<String> selectedIds = new CopyOnWriteArrayList<>();
//    select(1, selectSeq, selected, selectedIds);
//    Awaitility.await().until(() -> selected.size() == 1);
//    //关闭
//    AtomicBoolean closeCompleted = new AtomicBoolean();
//    closeService(selectedIds.get(0), closeCompleted);
//    Awaitility.await().until(() ->closeCompleted.get());
//
//    AtomicBoolean complete2 = new AtomicBoolean();
//    vertx.eventBus().<JsonObject>send("service.discovery.queryForNames", new JsonObject(), ar -> {
//      if (ar.failed()) {
//        ar.cause().printStackTrace();
//        return;
//      }
//      System.out.println(ar.result().body());
//      testContext.assertEquals(2, ar.result().body().size());
//      testContext.assertEquals(1, ar.result().body().getJsonObject("test").getInteger("instances"));
//      testContext.assertEquals(1, ar.result().body().getJsonObject("test2").getInteger
//              ("instances"));
//      complete2.set(true);
//    });
//
//    Awaitility.await().until(() -> complete2.get());
//  }
//
//  private void select(int count, AtomicInteger seq, List<Integer> selected, List<String>
//          selectedIds) {
//    for (int i = 0; i < count; i++) {
//      moreServiceDiscovery.queryForInstance("test", ar -> {
//        if (ar.succeeded()) {
//          int port = ar.result().getLocation().getInteger("port");
//          selected.add(port);
//          String id = ar.result().getRegistration();
//          selectedIds.add(id);
//        } else {
//          ar.cause().printStackTrace();
//        }
//        seq.incrementAndGet();
//      });
//    }
//  }
//
//  private void closeService(String id, AtomicBoolean complete) {
//    discovery.unpublish(id, ar -> {
//      complete.set(true);
//    });
//  }
//
//  private class ClusterDiscovery  {
//    private Vertx clusterVertx;
//
//    public ClusterDiscovery(List<Record> records, AtomicInteger seq) {
//      Runnable runnable = () -> {
//        Vertx.clusteredVertx(new VertxOptions().setClustered(true), ar-> {
//          clusterVertx = ar.result();
//          clusterVertx
//                  .deployVerticle(new AbstractVerticle() {
//                    @Override
//                    public void start() throws Exception {
//                      ServiceDiscovery serviceDiscovery = ServiceDiscovery.create(vertx);
//                      for (Record record : records) {
//                        serviceDiscovery.publish(record,
//                                                 ar -> seq.incrementAndGet());
//                      }
//                    }
//                  });
//        });
//      };
//
//      new Thread(runnable).start();
//    }
//
//    public void close(AtomicBoolean complete) {
//      clusterVertx.close(ar -> {
//        complete.set(true);
//      });
//    }
//  }
//}