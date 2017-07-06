package com.edgar.servicediscovery;

import io.vertx.servicediscovery.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 基于权重的随机轮询策略.
 * <p>
 * 算法来自nginx: https://github.com/phusion/nginx/commit/27e94984486058d73157038f7950a0a36ecc6e35
 * <p>
 * <p>
 * Upstream: smooth weighted round-robin balancing.
 * <p>
 * For edge case weights like { 5, 1, 1 } we now produce { a, a, b, a, c, a, a }
 * sequence instead of { c, b, a, a, a, a, a } produced previously.
 * <p>
 * Algorithm is as follows: on each peer selection we increase current_weight
 * of each eligible peer by its weight, select peer with greatest current_weight
 * and reduce its current_weight by total number of weight points distributed
 * among peers.
 * <p>
 * In case of { 5, 1, 1 } weights this gives the following sequence of
 * current_weight's:
 * <p>
 * a  b  c
 * 0  0  0  (initial state)
 * <p>
 * 5  1  1  (a selected)
 * -2  1  1
 * <p>
 * 3  2  2  (a selected)
 * -4  2  2
 * <p>
 * 1  3  3  (b selected)
 * 1 -4  3
 * <p>
 * 6 -3  4  (a selected)
 * -1 -3  4
 * <p>
 * 4 -2  5  (c selected)
 * 4 -2 -2
 * <p>
 * 9 -1 -1  (a selected)
 * 2 -1 -1
 * <p>
 * 7  0  0  (a selected)
 * 0  0  0
 * <p>
 * To preserve weight reduction in case of failures the effective_weight
 * variable was introduced, which usually matches peer's weight, but is
 * reduced temporarily on peer failures.
 * Created by edgar on 17-5-6.
 */
class WeightRoundbinStrategy implements ProviderStrategy {

  private final Lock lock = new ReentrantLock();

  private Map<String, Integer> effectiveWeightMap = new ConcurrentHashMap<>();

  @Override
  public Record get(List<Record> instances) {
    List<EffectiveWeighInstance> effectiveWeighInstances =
            instances.stream()
                    .map(i -> new EffectiveWeighInstance(i.getRegistration(), getWeight(i),
                                                         getEffectiveWeight(i)))
                    .collect(Collectors.toList());
    EffectiveWeighInstance instance = compute(effectiveWeighInstances);
    //更新权重
    effectiveWeighInstances.forEach(i -> {
      effectiveWeightMap.compute(i.id, (s, oldValue) -> {
        if (oldValue == null) {
          return i.changedWeight();
        }
        return oldValue + i.changedWeight();
      });
    });
    //减少这个方法的执行次数，只要保证effectiveWeightMap不会造成溢出即可
    if (effectiveWeightMap.size() % 1000 == 0) {
      if (lock.tryLock()) {
        //删除已经不存在的节点
        try {
          remoteNotExistsNode(instances);
        } finally {
          lock.unlock();
        }
      }
    }
    return instances.stream()
            .filter(i -> i.getRegistration().equals(instance.id))
            .findFirst()
            .get();
  }

  private void remoteNotExistsNode(List<Record> instances) {
    List<String> existsIds = instances.stream()
            .map(i -> i.getRegistration())
            .collect(Collectors.toList());
    List<String> ids = new ArrayList<>(effectiveWeightMap.keySet());
    List<String> notExistsIds = ids.stream()
            .filter(id -> !existsIds.contains(id))
            .collect(Collectors.toList());
    notExistsIds.forEach(id -> effectiveWeightMap.remove(id));
  }

  private int getWeight(Record record) {
    return record.getMetadata().getInteger("weight", 60);
  }

  private int getEffectiveWeight(Record record) {
    return effectiveWeightMap
            .getOrDefault(record.getRegistration(), getWeight(record));
  }

  private EffectiveWeighInstance compute(List<EffectiveWeighInstance> instances) {
    int total = instances.stream()
            .map(r -> r.weight())
            .reduce(0, (i1, i2) -> i1 + i2);

    instances.stream()
            .forEach(i -> i.incEffectiveWeight(i.weight()));
    EffectiveWeighInstance instance = instances.stream()
            .max((o1, o2) -> o1.effectiveWeight() - o2.effectiveWeight())
            .get();
    Collections.sort(new ArrayList<>(instances), (o1, o2) -> o1.weight() - o2.weight());
    return instance.decEffectiveWeight(total);
  }

  private class EffectiveWeighInstance {
    private final String id;

    private final int weight;

    private final AtomicInteger effectiveWeight;

    private final AtomicInteger changedWeight = new AtomicInteger();

    private EffectiveWeighInstance(String id, int weight, int effectiveWeight) {
      this.id = id;
      this.weight = weight;
      this.effectiveWeight = new AtomicInteger(effectiveWeight);
    }

    public int weight() {
      return weight;
    }

    public int effectiveWeight() {
      return effectiveWeight.get();
    }

    public int changedWeight() {
      return changedWeight.get();
    }

    @Override
    public String toString() {
      return "EffectiveWeighInstance{" +
             "id='" + id + '\'' +
             ", weight=" + weight +
             ", effectiveWeight=" + effectiveWeight +
             ", changedWeight=" + changedWeight +
             '}';
    }

    EffectiveWeighInstance incEffectiveWeight(int num) {
      effectiveWeight.accumulateAndGet(num, (left, right) -> left + right);
      changedWeight.accumulateAndGet(num, (left, right) -> left + right);
      return this;
    }

    EffectiveWeighInstance decEffectiveWeight(int num) {
      effectiveWeight.accumulateAndGet(num, (left, right) -> left - right);
      changedWeight.accumulateAndGet(num, (left, right) -> left - right);
      return this;
    }
  }

}
