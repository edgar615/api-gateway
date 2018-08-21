package com.github.edgar615.gateway.http.loadbalance;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2017/7/31.
 *
 * @author Edgar  Date 2017/7/31
 */
public class LoadBalanceTest {

    private Vertx vertx;

    private ServiceFinder serviceFinder;

    private ServiceDiscovery discovery;

    private LoadBalance loadBalance;

    private String service;

    private List<ServiceFilter> serviceFilters;

    @Before
    public void setUp() {
        service = UUID.randomUUID().toString();
        vertx = Vertx.vertx();
        discovery = ServiceDiscovery.create(vertx);
        serviceFinder = ServiceFinder.create(vertx, discovery);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        serviceFilters = new ArrayList<>();
        serviceFilters.add(r -> !LoadBalanceStats.instance().get(r.getRegistration())
                .isCircuitBreakerTripped());
    }

    @Test
    public void testDefault() {
        loadBalance = LoadBalance.create(serviceFinder, new LoadBalanceOptions());
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
        loadBalance = LoadBalance.create(serviceFinder, new LoadBalanceOptions()
                .addStrategy(service, ChooseStrategy.random()));

        AtomicInteger seq = new AtomicInteger();
        addService(seq);

        Awaitility.await().until(() -> seq.get() == 3);

        AtomicInteger selectSeq = new AtomicInteger();
        List<Integer> selected = new CopyOnWriteArrayList<>();
        List<String> selectedIds = new CopyOnWriteArrayList<>();
        select(3000, selectSeq, selected, selectedIds);

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
        loadBalance = LoadBalance.create(serviceFinder, new LoadBalanceOptions()
                .addStrategy(service, ChooseStrategy.roundRobin()));
        AtomicInteger seq = new AtomicInteger();
        addService(seq);

        Awaitility.await().until(() -> seq.get() == 3);

        AtomicInteger selectSeq = new AtomicInteger();
        List<Integer> selected = new CopyOnWriteArrayList<>();
        List<String> selectedIds = new CopyOnWriteArrayList<>();
        select(3000, selectSeq, selected, selectedIds);

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
        loadBalance = LoadBalance.create(serviceFinder, new LoadBalanceOptions()
                .addStrategy(service, ChooseStrategy.sticky(ChooseStrategy.random())));

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
        loadBalance = LoadBalance.create(serviceFinder, new LoadBalanceOptions()
                .addStrategy(service, ChooseStrategy.weightRoundRobin()));

        AtomicInteger seq = new AtomicInteger();
        addService(seq);

        Awaitility.await().until(() -> seq.get() == 3);

        AtomicInteger selectSeq = new AtomicInteger();
        List<Integer> selected = new CopyOnWriteArrayList<>();
        List<String> selectedIds = new CopyOnWriteArrayList<>();
        select(3000, selectSeq, selected, selectedIds);

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
        loadBalance = LoadBalance.create(serviceFinder, new LoadBalanceOptions()
                .addStrategy(service, ChooseStrategy.weightRoundRobin()));

        AtomicInteger seq = new AtomicInteger();
        addWeightService(seq);

        Awaitility.await().until(() -> seq.get() == 3);

        AtomicInteger selectSeq = new AtomicInteger();
        List<Integer> selected = new CopyOnWriteArrayList<>();
        List<String> selectedIds = new CopyOnWriteArrayList<>();
        select(7000, selectSeq, selected, selectedIds);

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

    @Test
    public void testCircuitBreakerTripped() {
        loadBalance = LoadBalance.create(serviceFinder, new LoadBalanceOptions()
                .addStrategy(service, ChooseStrategy.roundRobin()));

        AtomicInteger seq = new AtomicInteger();
        addService(seq);

        Awaitility.await().until(() -> seq.get() == 3);

        AtomicInteger selectSeq = new AtomicInteger();
        List<Integer> selected = new CopyOnWriteArrayList<>();
        List<String> selectedIds = new CopyOnWriteArrayList<>();
        select(3000, selectSeq, selected, selectedIds);

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

        LoadBalanceStats.instance().get(selectedIds.get(0)).setCircuitBreakerTripped(true);

        AtomicInteger selectSeq2 = new AtomicInteger();
        List<Integer> selected2 = new CopyOnWriteArrayList<>();
        List<String> selectedIds2 = new CopyOnWriteArrayList<>();
        select(3000, selectSeq2, selected2, selectedIds2);

        Awaitility.await().until(() -> selectSeq2.get() == 3000);
        Assert.assertEquals(2, new HashSet<>(selected2).size());
        Assert.assertEquals(2, new HashSet<>(selectedIds2).size());

    }


    private void select(int count, AtomicInteger seq, List<Integer> selected, List<String>
            selectedIds) {
        for (int i = 0; i < count; i++) {
            loadBalance.chooseServer(service, serviceFilters, ar -> {
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

    private void selectSticky3000(AtomicInteger seq, List<Integer> selected, List<String>
            selectedIds) {
        for (int i = 0; i < 500; i++) {
            loadBalance.chooseServer(service, serviceFilters, ar -> {
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
        discovery.getRecords(r -> service.equals(r.getName()), ar -> {
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
            loadBalance.chooseServer(service, ar -> {
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
        discovery.publish(HttpEndpoint.createRecord(service, "localhost", first, "/"),
                          ar -> published
                                  .set(true));

        Awaitility.await().until(() -> published.get());

        for (int i = 0; i < 1500; i++) {
            loadBalance.chooseServer(service, ar -> {
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
        discovery.publish(HttpEndpoint.createRecord(service, "localhost", 8081, "/"),
                          ar -> seq.incrementAndGet());
        discovery.publish(HttpEndpoint.createRecord(service, "localhost", 8082, "/"),
                          ar -> seq.incrementAndGet());
        discovery.publish(HttpEndpoint.createRecord(service, "localhost", 8083, "/"),
                          ar -> seq.incrementAndGet());
    }

    private void addWeightService(AtomicInteger seq) {
        discovery.publish(HttpEndpoint.createRecord(service, "localhost", 8081, "/"),
                          ar -> {
                              LoadBalanceStats.instance().get(ar.result().getRegistration())
                                      .setWeight(5);
                              seq.incrementAndGet();
                          });
        discovery.publish(HttpEndpoint.createRecord(service, "localhost", 8082, "/"),
                          ar -> {
                              LoadBalanceStats.instance().get(ar.result().getRegistration())
                                      .setWeight(1);
                              seq.incrementAndGet();
                          });
        discovery.publish(HttpEndpoint.createRecord(service, "localhost", 8083, "/"),
                          ar -> {
                              LoadBalanceStats.instance().get(ar.result().getRegistration())
                                      .setWeight(1);
                              seq.incrementAndGet();
                          });
    }
}
