package com.edgar.direwolves.discovery;

import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by edgar on 17-5-6.
 */
public class WeightRoundRobinStrategyTest {

  List<ServiceInstance> instances = new ArrayList<>();

  @Before
  public void setUp() {
    instances.clear();
    instances.add(new ServiceInstance(
            HttpEndpoint.createRecord("device", "localhost", 8080, "/").setRegistration("a"), 5));
    instances.add(new ServiceInstance(HttpEndpoint.createRecord("device", "localhost", 8080, "/")
                                              .setRegistration("b"), 1));
    instances.add(new ServiceInstance(HttpEndpoint.createRecord("device", "localhost", 8080, "/")
                                              .setRegistration("c"), 1));
  }


  @Test
  public void testWeight() {
    ProviderStrategy providerStrategy = ProviderStrategy.weightRoundRobin();
    List<String> selected = select(providerStrategy);
    Assert.assertEquals(3, new HashSet<>(selected).size());
    long aSize = selected.stream()
            .filter(i -> "a".equals(i))
            .count();
    long bSize = selected.stream()
            .filter(i -> "b".equals(i))
            .count();
    long cSize = selected.stream()
            .filter(i -> "c".equals(i))
            .count();
    Assert.assertEquals(aSize, 5000);
    Assert.assertEquals(bSize, 1000);
    Assert.assertEquals(cSize, 1000);
  }

  @Test
  public void testSmoothWeight() {
    ProviderStrategy providerStrategy = ProviderStrategy.weightRoundRobin();
    List<String> selected = select7(providerStrategy);
    System.out.println(selected);
    Assert.assertEquals(3, new HashSet<>(selected).size());
    long aSize = selected.stream()
            .filter(i -> "a".equals(i))
            .count();
    long bSize = selected.stream()
            .filter(i -> "b".equals(i))
            .count();
    long cSize = selected.stream()
            .filter(i -> "c".equals(i))
            .count();
    Assert.assertEquals(aSize, 5);
    Assert.assertEquals(bSize, 1);
    Assert.assertEquals(cSize, 1);

    Assert.assertEquals("a", selected.get(0));
    Assert.assertEquals("a", selected.get(1));
    Assert.assertEquals("b", selected.get(2));
    Assert.assertEquals("a", selected.get(3));
    Assert.assertEquals("c", selected.get(4));
    Assert.assertEquals("a", selected.get(5));
    Assert.assertEquals("a", selected.get(6));
  }

  @Test
  public void testDowngrade() {
    ProviderStrategy providerStrategy = ProviderStrategy.weightRoundRobin();
    List<String> selected = selectWithDowngrade(providerStrategy);
    System.out.println(selected);
    Assert.assertEquals(3, new HashSet<>(selected).size());
    long aSize = selected.stream()
            .filter(i -> "a".equals(i))
            .count();
    long bSize = selected.stream()
            .filter(i -> "b".equals(i))
            .count();
    long cSize = selected.stream()
            .filter(i -> "c".equals(i))
            .count();
    Assert.assertEquals(aSize, 7);
    Assert.assertEquals(bSize, 3);
    Assert.assertEquals(cSize, 5);

    Assert.assertEquals("a", selected.get(0));
    Assert.assertEquals("a", selected.get(1));
    Assert.assertEquals("b", selected.get(2));
    Assert.assertEquals("a", selected.get(3));
    Assert.assertEquals("c", selected.get(4));
    Assert.assertEquals("a", selected.get(5));
    Assert.assertEquals("a", selected.get(6));
    Assert.assertEquals("a", selected.get(7));
    Assert.assertEquals("b", selected.get(8));
    Assert.assertEquals("c", selected.get(9));
    Assert.assertEquals("c", selected.get(10));
    Assert.assertEquals("a", selected.get(11));
    Assert.assertEquals("c", selected.get(12));
    Assert.assertEquals("b", selected.get(13));
    Assert.assertEquals("c", selected.get(14));
  }

  private List<String> select7(ProviderStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      ServiceInstance instance = strategy.get(instances);
      selected.add(instance.id());
    }
    return selected;
  }

  private List<String> select(ProviderStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 7000; i++) {
      ServiceInstance instance = strategy.get(instances);
      selected.add(instance.id());
    }
    return selected;
  }

  private List<String> selectWithDowngrade(ProviderStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      ServiceInstance instance = strategy.get(instances);
      selected.add(instance.id());
    }
    //将a降级到1，选择3次，应该a,b,c平均
    instances.stream()
            .filter(i -> i.id().equals("a"))
            .forEach(i -> i.decWeight(4));
    for (int i = 0; i < 3; i++) {
      ServiceInstance instance = strategy.get(instances);
      selected.add(instance.id());
    }

    //将C升级到3，选择5次，应该 c, a, c, b c
    instances.stream()
            .filter(i -> i.id().equals("c"))
            .forEach(i -> i.incWeight(2));
    for (int i = 0; i < 5; i++) {
      ServiceInstance instance = strategy.get(instances);
      selected.add(instance.id());
    }
    return selected;
  }

}
