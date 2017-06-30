//package com.edgar.service.discovery;
//
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
//import java.util.Random;
//import java.util.Set;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * Created by Edgar on 2017/5/10.
// *
// * @author Edgar  Date 2017/5/10
// */
//@RunWith(VertxUnitRunner.class)
//public class ServiceDiscoveryClusterTest {
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
////    vertx.eventBus().consumer("vertx.discovery.announce", ar -> {
////      System.out.println(ar.body());
////    });
//  }
//
//  @After
//  public void tearDown() {
//    AtomicBoolean complete = new AtomicBoolean();
//    vertx.close( ar -> {
//      complete.set(true);
//    });
//    Awaitility.await().until(() -> complete.get());
//  }
//
//  @After
//  public void tearDwon() {
//    AtomicBoolean complete = new AtomicBoolean();
//    vertx.close(ar -> {
//      complete.set(true);
//    });
//    Awaitility.await().until(() -> complete.get());
//
//  }
//
//  @Test
//  public void testDefault() {
//    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions());
//
//    AtomicInteger seq = new AtomicInteger();
//    List<Record> records = new ArrayList<>();
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8081, "/"));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8082, "/"));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8083, "/"));
//
//    ClusterDiscovery clusterDiscovery = new ClusterDiscovery(records, seq);
//    Awaitility.await().until(() -> seq.get() == records.size());
//
//    AtomicInteger selectSeq = new AtomicInteger();
//    List<Integer> selected = new CopyOnWriteArrayList<>();
//    List<String> selectedIds = new CopyOnWriteArrayList<>();
//    select(3000, selectSeq, selected, selectedIds);
//
//    Awaitility.await().until(() -> selected.size() == 3000);
//   Set<Integer> test = new HashSet<>(selected);
//           test.remove(null);
//    Assert.assertEquals(3, new HashSet<>(test).size());
//    Assert.assertEquals(3, new HashSet<>(selectedIds).size());
//    System.out.println( new HashSet<>(selectedIds));
//    long aSize = selected.stream()
//            .filter(i -> 8081 == i)
//            .count();
//    long bSize = selected.stream()
//            .filter(i -> 8082 == i)
//            .count();
//    long cSize = selected.stream()
//            .filter(i -> 8083 == i)
//            .count();
//    Assert.assertEquals(aSize, 1000);
//    Assert.assertEquals(bSize, 1000);
//    Assert.assertEquals(cSize, 1000);
//
//    AtomicBoolean complete = new AtomicBoolean();
//    clusterDiscovery.close(complete);
//    Awaitility.await().until(() ->complete.get());
//  }
//
//  @Test
//  public void testRandom() {
//    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions()
//            .addStrategy("test", "random"));
//
//    AtomicInteger seq = new AtomicInteger();
//    List<Record> records = new ArrayList<>();
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8081, "/"));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8082, "/"));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8083, "/"));
//
//    ClusterDiscovery clusterDiscovery = new ClusterDiscovery(records, seq);
//    Awaitility.await().until(() -> seq.get() == records.size());
//
//    AtomicInteger selectSeq = new AtomicInteger();
//    List<Integer> selected = new CopyOnWriteArrayList<>();
//    List<String> selectedIds = new CopyOnWriteArrayList<>();
//    select(3000,selectSeq, selected, selectedIds);
//
//    Awaitility.await().until(() -> selectSeq.get() == 3000);
//    Assert.assertEquals(3, new HashSet<>(selected).size());
//    Assert.assertEquals(3, new HashSet<>(selectedIds).size());
//    long aSize = selected.stream()
//            .filter(i -> 8081 == i)
//            .count();
//    long bSize = selected.stream()
//            .filter(i -> 8082 == i)
//            .count();
//    long cSize = selected.stream()
//            .filter(i -> 8083 == i)
//            .count();
//    Assert.assertFalse(aSize == 1000 && bSize == 1000 && cSize == 1000);
//
//    AtomicBoolean complete = new AtomicBoolean();
//    clusterDiscovery.close(complete);
//    Awaitility.await().until(() ->complete.get());
//  }
//
//
//  @Test
//  public void testRoundRobin() {
//    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions()
//            .addStrategy("test", "round_robin"));
//
//    AtomicInteger seq = new AtomicInteger();
//    List<Record> records = new ArrayList<>();
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8081, "/"));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8082, "/"));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8083, "/"));
//
//    ClusterDiscovery clusterDiscovery = new ClusterDiscovery(records, seq);
//    Awaitility.await().until(() -> seq.get() == records.size());
//
//    AtomicInteger selectSeq = new AtomicInteger();
//    List<Integer> selected = new CopyOnWriteArrayList<>();
//    List<String> selectedIds = new CopyOnWriteArrayList<>();
//    select(3000,selectSeq, selected, selectedIds);
//
//    Awaitility.await().until(() -> selectSeq.get() == 3000);
//    Assert.assertEquals(3, new HashSet<>(selected).size());
//    Assert.assertEquals(3, new HashSet<>(selectedIds).size());
//    long aSize = selected.stream()
//            .filter(i -> 8081 == i)
//            .count();
//    long bSize = selected.stream()
//            .filter(i -> 8082 == i)
//            .count();
//    long cSize = selected.stream()
//            .filter(i -> 8083 == i)
//            .count();
//    Assert.assertEquals(aSize, 1000);
//    Assert.assertEquals(bSize, 1000);
//    Assert.assertEquals(cSize, 1000);
//
//    AtomicBoolean complete = new AtomicBoolean();
//    clusterDiscovery.close(complete);
//    Awaitility.await().until(() ->complete.get());
//  }
//
//  @Test
//  public void testSticky() {
//    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions()
//            .addStrategy("test", "sticky"));
//
//    AtomicInteger seq = new AtomicInteger();
//    List<Record> records = new ArrayList<>();
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8081, "/"));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8082, "/"));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8083, "/"));
//
//    ClusterDiscovery clusterDiscovery = new ClusterDiscovery(records, seq);
//    Awaitility.await().until(() -> seq.get() == records.size());
//
//    AtomicInteger selectSeq = new AtomicInteger();
//    List<Integer> selected = new CopyOnWriteArrayList<>();
//    List<String> selectedIds = new CopyOnWriteArrayList<>();
//
//    selectSticky3000(selectSeq, selected, selectedIds);
//    Awaitility.await().until(() -> selectSeq.get() == 3000);
//    Assert.assertEquals(2, new HashSet<>(selected).size());
//    Assert.assertEquals(2, new HashSet<>(selectedIds).size());
//    long aSize = selected.stream()
//            .filter(i -> selected.get(0) == i)
//            .count();
//    long bSize = selected.stream()
//            .filter(i -> selected.get(2000) == i)
//            .count();
//    Assert.assertEquals(aSize, 500);
//    Assert.assertEquals(bSize, 2500);
//
//    AtomicBoolean complete = new AtomicBoolean();
//    clusterDiscovery.close(complete);
//    Awaitility.await().until(() ->complete.get());
//  }
//
//  @Test
//  public void testWeightEquilibrium() {
//    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions()
//            .addStrategy("test", "weight_round_robin"));
//
//    AtomicInteger seq = new AtomicInteger();
//    List<Record> records = new ArrayList<>();
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8081, "/"));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8082, "/"));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8083, "/"));
//
//    ClusterDiscovery clusterDiscovery = new ClusterDiscovery(records, seq);
//    Awaitility.await().until(() -> seq.get() == records.size());
//
//    AtomicInteger selectSeq = new AtomicInteger();
//    List<Integer> selected = new CopyOnWriteArrayList<>();
//    List<String> selectedIds = new CopyOnWriteArrayList<>();
//    select(3000,selectSeq, selected, selectedIds);
//
//    Awaitility.await().until(() -> selectSeq.get() == 3000);
//    System.out.println(selected);
//    Assert.assertEquals(3, new HashSet<>(selected).size());
//    Assert.assertEquals(3, new HashSet<>(selectedIds).size());
//    long aSize = selected.stream()
//            .filter(i -> 8081 == i)
//            .count();
//    long bSize = selected.stream()
//            .filter(i -> 8082 == i)
//            .count();
//    long cSize = selected.stream()
//            .filter(i -> 8083 == i)
//            .count();
//    Assert.assertEquals(aSize, 1000);
//    Assert.assertEquals(bSize, 1000);
//    Assert.assertEquals(cSize, 1000);
//
//    AtomicBoolean complete = new AtomicBoolean();
//    clusterDiscovery.close(complete);
//    Awaitility.await().until(() ->complete.get());
//  }
//
//  @Test
//  public void testWeightDisequilibrium() {
//    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions()
//            .addStrategy("test", "weight_round_robin"));
//
//    AtomicInteger seq = new AtomicInteger();
//    List<Record> records = new ArrayList<>();
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8081, "/").setMetadata(new
//                                                                                              JsonObject().put("weight", 50)));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8082, "/").setMetadata(new
//                                                                                              JsonObject().put("weight", 10)));
//    records.add(HttpEndpoint.createRecord("test", "localhost", 8083, "/").setMetadata(new
//                                                                                              JsonObject().put("weight", 10)));
//
//    ClusterDiscovery clusterDiscovery = new ClusterDiscovery(records, seq);
//    Awaitility.await().until(() -> seq.get() == records.size());
//
//    AtomicInteger selectSeq = new AtomicInteger();
//    List<Integer> selected = new CopyOnWriteArrayList<>();
//    List<String> selectedIds = new CopyOnWriteArrayList<>();
//    select(7000,selectSeq, selected, selectedIds);
//
//    Awaitility.await().until(() -> selectSeq.get() == 7000);
//    Assert.assertEquals(3, new HashSet<>(selected).size());
//    Assert.assertEquals(3, new HashSet<>(selectedIds).size());
//    long aSize = selected.stream()
//            .filter(i -> 8081 == i)
//            .count();
//    long bSize = selected.stream()
//            .filter(i -> 8082 == i)
//            .count();
//    long cSize = selected.stream()
//            .filter(i -> 8083 == i)
//            .count();
//    Assert.assertEquals(aSize, 5000);
//    Assert.assertEquals(bSize, 1000);
//    Assert.assertEquals(cSize, 1000);
//    AtomicBoolean complete = new AtomicBoolean();
//    clusterDiscovery.close(complete);
//    Awaitility.await().until(() ->complete.get());
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
//  private void selectSticky3000(AtomicInteger seq, List<Integer> selected, List<String> selectedIds) {
//    for (int i = 0; i < 500; i++) {
//      moreServiceDiscovery.queryForInstance("test", ar -> {
//        if (ar.succeeded()) {
//          selected.add(ar.result().getLocation().getInteger("port"));
//          selectedIds.add(ar.result().getRegistration());
//        } else {
//          ar.cause().printStackTrace();
//        }
//        seq.incrementAndGet();
//      });
//    }
//    Awaitility.await().until(() -> seq.get() == 500);
//    int first = selected.get(0);
//
//    AtomicBoolean unpublished = new AtomicBoolean();
//    moreServiceDiscovery.queryAllInstances("test", ar -> {
//      List<Record> instances = ar.result();
//      String firstId = instances.stream()
//              .filter(i -> i.getLocation().getInteger("port") == first)
//              .findFirst()
//              .get().getRegistration();
//      discovery.unpublish(firstId, ar2 -> {
//        unpublished.set(true);
//      });
//    });
//
//    Awaitility.await().until(() -> unpublished.get());
//
//    for (int i = 0; i < 1000; i++) {
//      moreServiceDiscovery.queryForInstance("test", ar -> {
//        if (ar.succeeded()) {
//          selected.add(ar.result().getLocation().getInteger("port"));
//          selectedIds.add(ar.result().getRegistration());
//        } else {
//          ar.cause().printStackTrace();
//        }
//        seq.incrementAndGet();
//      });
//    }
//    Awaitility.await().until(() -> seq.get() == 1500);
//
//    AtomicBoolean published = new AtomicBoolean();
//    discovery.publish(HttpEndpoint.createRecord("test", "localhost", first, "/"), ar -> published
//            .set(true));
//
//    Awaitility.await().until(() -> published.get());
//
//    for (int i = 0; i < 1500; i++) {
//      moreServiceDiscovery.queryForInstance("test", ar -> {
//        if (ar.succeeded()) {
//          selected.add(ar.result().getLocation().getInteger("port"));
//          selectedIds.add(ar.result().getRegistration());
//        } else {
//          ar.cause().printStackTrace();
//        }
//        seq.incrementAndGet();
//      });
//    }
//    Awaitility.await().until(() -> seq.get() == 3000);
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
