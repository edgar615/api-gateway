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

  private Record record;

  public ServiceInstance(Record record) {
    this(record, 60);
  }


  public ServiceInstance(Record record, int weight) {
    this.id = record.getRegistration();
    this.name = record.getName();
    this.weight = new AtomicInteger(weight);
    this.record = record;
  }

  public Record record() {
    return record;
  }

  public ServiceInstance inc() {
    weight.incrementAndGet();
    return this;
  }

  public ServiceInstance dec() {
    weight.decrementAndGet();
    return this;
  }

  public ServiceInstance add(int num) {
    weight.addAndGet(num);
    return this;
  }

  public int weight() {
    return weight.get();
  }

  public String name() {
    return name;
  }

  public String id() {
    return id;
  }
}
