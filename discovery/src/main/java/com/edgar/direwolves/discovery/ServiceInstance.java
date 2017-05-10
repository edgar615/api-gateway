package com.edgar.direwolves.discovery;

import io.vertx.servicediscovery.Record;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by edgar on 17-5-9.
 */
public class ServiceInstance {

  private final String id;

  private final String name;

  private AtomicInteger weight;

  private AtomicInteger effectiveWeight;

  private Record record;

  public ServiceInstance(Record record) {
    this(record, 60);
  }


  public ServiceInstance(Record record, int weight) {
    this.id = record.getRegistration();
    this.name = record.getName();
    this.weight = new AtomicInteger(weight);
    this.record = record;
    this.effectiveWeight = new AtomicInteger(0);
  }

  public Record record() {
    return record;
  }

  public ServiceInstance incWeight(int num) {
    weight.accumulateAndGet(num, (left, right) -> Math.min(left + right, 100));
    return this;
  }

  public ServiceInstance decWeight(int num) {
    weight.accumulateAndGet(num, (left, right) -> Math.max(left - right, 0));
    return this;
  }

  public ServiceInstance incEffectiveWeight(int num) {
    effectiveWeight.accumulateAndGet(num, (left, right) -> left + right);
    return this;
  }

  public ServiceInstance decEffectiveWeight(int num) {
    effectiveWeight.accumulateAndGet(num, (left, right) -> left - right);
    return this;
  }

  public int weight() {
    return weight.get();
  }

  public int effectiveWeight() {
    return effectiveWeight.get();
  }

  public String name() {
    return name;
  }

  public String id() {
    return id;
  }
}
