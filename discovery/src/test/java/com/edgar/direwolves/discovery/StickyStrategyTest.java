package com.edgar.direwolves.discovery;

import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by edgar on 17-5-6.
 */
public class StickyStrategyTest extends StrategyTest {

  @Test
  public void testStick() {
    ProviderStrategy providerStrategy = ProviderStrategy.sticky(ProviderStrategy.roundRobin());
    List<String> selected = select3000(providerStrategy);
    Assert.assertEquals(1, new HashSet<>(selected).size());
    long aSize = selected.stream()
        .filter(i -> "a".equals(i))
        .count();
    long bSize = selected.stream()
        .filter(i -> "b".equals(i))
        .count();
    long cSize = selected.stream()
        .filter(i -> "c".equals(i))
        .count();
    Assert.assertEquals(aSize, 3000);
    Assert.assertEquals(bSize, 0);
    Assert.assertEquals(cSize, 0);
  }

  @Test
  public void testStickReset() {
    ProviderStrategy providerStrategy = ProviderStrategy.sticky(ProviderStrategy.roundRobin());
    List<String> selected = select(providerStrategy);
    Assert.assertEquals(2, new HashSet<>(selected).size());
    long aSize = selected.stream()
        .filter(i -> "a".equals(i))
        .count();
    long bSize = selected.stream()
        .filter(i -> "b".equals(i))
        .count();
    long cSize = selected.stream()
        .filter(i -> "c".equals(i))
        .count();
    Assert.assertEquals(aSize, 1000);
    Assert.assertEquals(bSize, 0);
    Assert.assertEquals(cSize, 2000);
  }

  protected List<String> select(ProviderStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      ServiceInstance instance = strategy.get(instances);
      selected.add(instance.id());
    }

    instances.removeIf(r -> r.id().equalsIgnoreCase("a"));
    for (int i = 1000; i < 2000; i++) {
      ServiceInstance instance = strategy.get(instances);
      selected.add(instance.id());
    }

    instances.add(new ServiceInstance(HttpEndpoint.createRecord("device", "localhost", 8080, "/").setRegistration("a")));
    for (int i = 2000; i < 3000; i++) {
      ServiceInstance instance = strategy.get(instances);
      selected.add(instance.id());
    }
    return selected;
  }

}
