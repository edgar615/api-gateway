package com.edgar.direwolves.discovery;

import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
 * Created by edgar on 17-5-6.
 */
public class StrategyTest {
  List<ServiceInstance> instances = new ArrayList<>();

  @Before
  public void setUp() {
    instances.clear();
    instances.add(new ServiceInstance(
            HttpEndpoint.createRecord("device", "localhost", 8080, "/").setRegistration("a")));
    instances.add(new ServiceInstance(
            HttpEndpoint.createRecord("device", "localhost", 8080, "/").setRegistration("b")));
    instances.add(new ServiceInstance(
            HttpEndpoint.createRecord("device", "localhost", 8080, "/").setRegistration("c")));
  }

  protected List<String> select100(ProviderStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 3000; i++) {
      ServiceInstance instance = strategy.get(instances);
      selected.add(instance.id());
    }
    return selected;
  }

}
