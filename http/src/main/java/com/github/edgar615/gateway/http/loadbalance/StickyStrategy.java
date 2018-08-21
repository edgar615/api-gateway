package com.github.edgar615.gateway.http.loadbalance;

import io.vertx.servicediscovery.Record;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 主从模式.
 * 一旦一个节点被选定，这个节点就会被作为一个主节点，其他节点作为备份节点，以后每次选择节点都会直接选择主节点．
 * <p>
 * 注意：如果主节点已经不在给定对列表中，那么会使用主节点的选择策略重新选择一个节点作为主节点
 * <p>
 * Created by edgar on 17-5-6.
 */
class StickyStrategy implements ChooseStrategy {

    private final ChooseStrategy masterStrategy;

    private final AtomicReference<Record> ourInstance = new AtomicReference<>(null);

    private final AtomicInteger instanceNumber = new AtomicInteger(-1);

    public StickyStrategy(ChooseStrategy masterStrategy) {
        this.masterStrategy = masterStrategy;
    }

    @Override
    public Record get(List<Record> instances) {

        Record localOurInstance = ourInstance.get();
        if (localOurInstance != null) {
            long count = instances.stream()
                    .filter(i -> i.getRegistration().equals(localOurInstance.getRegistration()))
                    .count();
            if (count == 0) {
                ourInstance.compareAndSet(localOurInstance, null);
            }
        }

        if (ourInstance.get() == null) {
            Record instance = masterStrategy.get(instances);
            if (ourInstance.compareAndSet(null, instance)) {
                instanceNumber.incrementAndGet();
            }
        }
        return ourInstance.get();
    }
}
