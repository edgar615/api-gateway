package com.edgar.servicediscovery;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2017/5/10.
 *
 * @author Edgar  Date 2017/5/10
 */
@RunWith(VertxUnitRunner.class)
public class ServiceDiscoveryTest {

  private Vertx vertx;

  private MoreServiceDiscovery moreServiceDiscovery;

  private ServiceDiscovery discovery;

  private int port;

  @Before
  public void setUp(TestContext testContext) {
    port = new Random().nextInt(30000);
    vertx = Vertx.vertx();
    discovery = ServiceDiscovery.create(vertx);
//    vertx.eventBus().consumer("vertx.discovery.announce", ar -> {
//      System.out.println(ar.body());
//    });
  }

  @Test
  public void testDefault() {
    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions());

    AtomicInteger seq = new AtomicInteger();
    addService(seq);

    Awaitility.await().until(() -> seq.get() == 3);

    AtomicInteger selectSeq = new AtomicInteger();
    List<Integer> selected = new CopyOnWriteArrayList<>();
    List<String> selectedIds = new CopyOnWriteArrayList<>();
    select(3000, selectSeq, selected, selectedIds);

    Awaitility.await().until(() -> selected.size() == 3000);
    Assert.assertEquals(3, new HashSet<>(selected).size());
    Assert.assertEquals(3, new HashSet<>(selectedIds).size());
    long aSize = selected.stream()
            .filter(i -> 8081 == i)
            .count();
    long bSize = selected.stream()
            .filter(i -> 8082 == i)
            .count();
    long cSize = selected.stream()
            .filter(i -> 8083 == i)
            .count();
    Assert.assertEquals(aSize, 1000);
    Assert.assertEquals(bSize, 1000);
    Assert.assertEquals(cSize, 1000);
  }

  @Test
  public void testRandom() {
    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions()
            .addStrategy("test", "random"));

    AtomicInteger seq = new AtomicInteger();
    addService(seq);

    Awaitility.await().until(() -> seq.get() == 3);

    AtomicInteger selectSeq = new AtomicInteger();
    List<Integer> selected = new CopyOnWriteArrayList<>();
    List<String> selectedIds = new CopyOnWriteArrayList<>();
    select(3000,selectSeq, selected, selectedIds);

    Awaitility.await().until(() -> selectSeq.get() == 3000);
    Assert.assertEquals(3, new HashSet<>(selected).size());
    Assert.assertEquals(3, new HashSet<>(selectedIds).size());
    long aSize = selected.stream()
            .filter(i -> 8081 == i)
            .count();
    long bSize = selected.stream()
            .filter(i -> 8082 == i)
            .count();
    long cSize = selected.stream()
            .filter(i -> 8083 == i)
            .count();
    Assert.assertFalse(aSize == 1000 && bSize == 1000 && cSize == 1000);
  }


  @Test
  public void testRoundRobin() {
    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions()
            .addStrategy("test", "round_robin"));

    AtomicInteger seq = new AtomicInteger();
    addService(seq);

    Awaitility.await().until(() -> seq.get() == 3);

    AtomicInteger selectSeq = new AtomicInteger();
    List<Integer> selected = new CopyOnWriteArrayList<>();
    List<String> selectedIds = new CopyOnWriteArrayList<>();
    select(3000,selectSeq, selected, selectedIds);

    Awaitility.await().until(() -> selectSeq.get() == 3000);
    Assert.assertEquals(3, new HashSet<>(selected).size());
    Assert.assertEquals(3, new HashSet<>(selectedIds).size());
    long aSize = selected.stream()
            .filter(i -> 8081 == i)
            .count();
    long bSize = selected.stream()
            .filter(i -> 8082 == i)
            .count();
    long cSize = selected.stream()
            .filter(i -> 8083 == i)
            .count();
    Assert.assertEquals(aSize, 1000);
    Assert.assertEquals(bSize, 1000);
    Assert.assertEquals(cSize, 1000);
  }

  @Test
  public void testSticky() {
    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions()
            .addStrategy("test", "sticky"));

    AtomicInteger seq = new AtomicInteger();
    addService(seq);

    Awaitility.await().until(() -> seq.get() == 3);

    AtomicInteger selectSeq = new AtomicInteger();
    List<Integer> selected = new CopyOnWriteArrayList<>();
    List<String> selectedIds = new CopyOnWriteArrayList<>();

    selectSticky3000(selectSeq, selected, selectedIds);
    Awaitility.await().until(() -> selectSeq.get() == 3000);
    Assert.assertEquals(2, new HashSet<>(selected).size());
    Assert.assertEquals(2, new HashSet<>(selectedIds).size());
    long aSize = selected.stream()
            .filter(i -> selected.get(0) == i)
            .count();
    long bSize = selected.stream()
            .filter(i -> selected.get(2000) == i)
            .count();
    Assert.assertEquals(aSize, 500);
    Assert.assertEquals(bSize, 2500);
  }

  @Test
  public void testWeightEquilibrium() {
    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions()
            .addStrategy("test", "weight_round_robin"));

    AtomicInteger seq = new AtomicInteger();
    addService(seq);

    Awaitility.await().until(() -> seq.get() == 3);

    AtomicInteger selectSeq = new AtomicInteger();
    List<Integer> selected = new CopyOnWriteArrayList<>();
    List<String> selectedIds = new CopyOnWriteArrayList<>();
    select(3000,selectSeq, selected, selectedIds);

    Awaitility.await().until(() -> selectSeq.get() == 3000);
    System.out.println(selected);
    Assert.assertEquals(3, new HashSet<>(selected).size());
    Assert.assertEquals(3, new HashSet<>(selectedIds).size());
    long aSize = selected.stream()
            .filter(i -> 8081 == i)
            .count();
    long bSize = selected.stream()
            .filter(i -> 8082 == i)
            .count();
    long cSize = selected.stream()
            .filter(i -> 8083 == i)
            .count();
    Assert.assertEquals(aSize, 1000);
    Assert.assertEquals(bSize, 1000);
    Assert.assertEquals(cSize, 1000);
  }

  @Test
  public void testWeightDisequilibrium() {
    moreServiceDiscovery = MoreServiceDiscovery.create(vertx, new MoreServiceDiscoveryOptions()
            .addStrategy("test", "weight_round_robin"));

    AtomicInteger seq = new AtomicInteger();
    addWeightService(seq);

    Awaitility.await().until(() -> seq.get() == 3);

    AtomicInteger selectSeq = new AtomicInteger();
    List<Integer> selected = new CopyOnWriteArrayList<>();
    List<String> selectedIds = new CopyOnWriteArrayList<>();
    select(7000,selectSeq, selected, selectedIds);

    Awaitility.await().until(() -> selectSeq.get() == 7000);
    Assert.assertEquals(3, new HashSet<>(selected).size());
    Assert.assertEquals(3, new HashSet<>(selectedIds).size());
    long aSize = selected.stream()
            .filter(i -> 8081 == i)
            .count();
    long bSize = selected.stream()
            .filter(i -> 8082 == i)
            .count();
    long cSize = selected.stream()
            .filter(i -> 8083 == i)
            .count();
    Assert.assertEquals(aSize, 5000);
    Assert.assertEquals(bSize, 1000);
    Assert.assertEquals(cSize, 1000);
    //刚开始的请求存在并发，并不是严格按照这个顺序
//    Assert.assertEquals(8081l, selected.get(0), 0);
//    Assert.assertEquals(8081l, selected.get(1), 0);
//    Assert.assertEquals(8082l, selected.get(2),0);
//    Assert.assertEquals(8081l, selected.get(3), 0);
//    Assert.assertEquals(8083l, selected.get(4), 0);
//    Assert.assertEquals(8081l, selected.get(5), 0);
//    Assert.assertEquals(8081l, selected.get(6), 0);
  }
//  @Test
//  public void testWeightCal() {
//    JsonObject strategyConfig = new JsonObject();
//    strategyConfig.put("test", new JsonObject().put("strategy", "weight_round_robin"));
//    discovery = ServiceDiscovery2
//            .create(vertx, new JsonObject().put("service.discovery.strategy", strategyConfig)
//                    .put("service.discovery.weight.timeout", 5000)
//                    .put("service.discovery.weight.increase", 5)
//                    .put("service.discovery.weight.decrease", 15));
//
//    addService();
//    Awaitility.await().until(() -> discovery.getInstances(r -> true).size() == 3);
//
//    ServiceDiscovery2 discovery2 = ServiceDiscovery2
//            .create(vertx, new JsonObject().put("service.discovery.strategy", strategyConfig)
//                    .put("service.discovery.weight.timeout", 5000)
//                    .put("service.discovery.weight.increase", 5)
//                    .put("service.discovery.weight.decrease", 15));
//
//    discovery2.complete("a", 1);
//    discovery2.complete("a", 1);
//    discovery2.complete("a", 1);
//    Assert.assertEquals(discovery.getInstances(r -> "a".equals(r.id()))
//                                .get(0).weight(), 75);
//
//    discovery2.complete("a", 5000);
//    Assert.assertEquals(discovery.getInstances(r -> "a".equals(r.id()))
//                                .get(0).weight(), 80);
//    discovery2.complete("a", 5001);
//    Assert.assertEquals(discovery.getInstances(r -> "a".equals(r.id()))
//                                .get(0).weight(), 65);
//
//    discovery2.fail("a");
//    Assert.assertEquals(discovery.getInstances(r -> "a".equals(r.id()))
//                                .get(0).weight(), 50);
//  }

  private void select(int count, AtomicInteger seq, List<Integer> selected, List<String>
          selectedIds) {
    for (int i = 0; i < count; i++) {
      moreServiceDiscovery.queryForInstance("test", ar -> {
        if (ar.succeeded()) {
          int port = ar.result().getLocation().getInteger("port");
          selected.add(port);
          String id = ar.result().getRegistration();
          selectedIds.add(id);
        } else {
          ar.cause().printStackTrace();
        }
        seq.incrementAndGet();
      });
    }
  }
  private void selectSticky3000(AtomicInteger seq, List<Integer> selected, List<String> selectedIds) {
    for (int i = 0; i < 500; i++) {
      moreServiceDiscovery.queryForInstance("test", ar -> {
        if (ar.succeeded()) {
          selected.add(ar.result().getLocation().getInteger("port"));
          selectedIds.add(ar.result().getRegistration());
        } else {
          ar.cause().printStackTrace();
        }
        seq.incrementAndGet();
      });
    }
    Awaitility.await().until(() -> seq.get() == 500);
    int first = selected.get(0);

    AtomicBoolean unpublished = new AtomicBoolean();
    moreServiceDiscovery.queryAllInstances("test", ar -> {
      List<Record> instances = ar.result();
      String firstId = instances.stream()
              .filter(i -> i.getLocation().getInteger("port") == first)
              .findFirst()
              .get().getRegistration();
      discovery.unpublish(firstId, ar2 -> {
        unpublished.set(true);
      });
    });

    Awaitility.await().until(() -> unpublished.get());

    for (int i = 0; i < 1000; i++) {
      moreServiceDiscovery.queryForInstance("test", ar -> {
        if (ar.succeeded()) {
          selected.add(ar.result().getLocation().getInteger("port"));
          selectedIds.add(ar.result().getRegistration());
        } else {
          ar.cause().printStackTrace();
        }
        seq.incrementAndGet();
      });
    }
    Awaitility.await().until(() -> seq.get() == 1500);

    AtomicBoolean published = new AtomicBoolean();
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", first, "/"), ar -> published
            .set(true));

    Awaitility.await().until(() -> published.get());

    for (int i = 0; i < 1500; i++) {
      moreServiceDiscovery.queryForInstance("test", ar -> {
        if (ar.succeeded()) {
          selected.add(ar.result().getLocation().getInteger("port"));
          selectedIds.add(ar.result().getRegistration());
        } else {
          ar.cause().printStackTrace();
        }
        seq.incrementAndGet();
      });
    }
    Awaitility.await().until(() -> seq.get() == 3000);
  }

  private void addService(AtomicInteger seq) {
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8081, "/"),
                      ar -> seq.incrementAndGet());
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8082, "/"),
                      ar -> seq.incrementAndGet());
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8083, "/"),
                      ar -> seq.incrementAndGet());
  }

  private void addWeightService(AtomicInteger seq) {
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8081, "/").setMetadata(new
                                                                                                    JsonObject().put("weight", 5)),
                      ar -> seq.incrementAndGet());
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8082, "/").setMetadata(new
                                                                                                    JsonObject().put("weight", 1)),
                      ar -> seq.incrementAndGet());
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8083, "/").setMetadata(new
                                                                                                    JsonObject().put("weight", 1)),
                      ar -> seq.incrementAndGet());
  }
}
