package com.github.edgar615.gateway.http.loadbalance;

import io.vertx.servicediscovery.Record;
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

  List<Record> instances = new ArrayList<>();

  @Before
  public void setUp() {
    instances.clear();
    Record a = HttpEndpoint.createRecord("test", "localhost", 8081, "/").setRegistration("a");
    LoadBalanceStats.instance().get("a").setWeight(5);
    Record b = HttpEndpoint.createRecord("test", "localhost", 8082, "/").setRegistration("b");
    LoadBalanceStats.instance().get("b").setWeight(1);
    Record c = HttpEndpoint.createRecord("test", "localhost", 8083, "/").setRegistration("c");
    LoadBalanceStats.instance().get("c").setWeight(1);
    instances.add(a);
    instances.add(b);
    instances.add(c);
  }

  @Test
  public void testWeight() {
    ChooseStrategy chooseStrategy = ChooseStrategy.weightRoundRobin();
    List<String> selected = select(chooseStrategy);
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
    ChooseStrategy chooseStrategy = ChooseStrategy.weightRoundRobin();
    List<String> selected = select7(chooseStrategy);
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
    ChooseStrategy chooseStrategy = ChooseStrategy.weightRoundRobin();
    List<String> selected = selectWithDowngrade(chooseStrategy);
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

  private List<String> select7(ChooseStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      Record instance = strategy.get(instances);
      selected.add(instance.getRegistration());
    }
    return selected;
  }

  private List<String> select(ChooseStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 7000; i++) {
      Record instance = strategy.get(instances);
      selected.add(instance.getRegistration());
    }
    return selected;
  }

  private List<String> selectWithDowngrade(ChooseStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      Record instance = strategy.get(instances);
      selected.add(instance.getRegistration());
    }
    //将a降级到1，选择3次，应该a,b,c平均
    instances.stream()
            .filter(i -> i.getRegistration().equals("a"))
            .forEach(i -> decWeight(i, 4));
    for (int i = 0; i < 3; i++) {
      Record instance = strategy.get(instances);
      selected.add(instance.getRegistration());
    }

    //将C升级到3，选择5次，应该 c, a, c, b c
    instances.stream()
            .filter(i -> i.getRegistration().equals("c"))
            .forEach(i -> incWeight(i, 2));
    for (int i = 0; i < 5; i++) {
      Record instance = strategy.get(instances);
      selected.add(instance.getRegistration());
    }
    return selected;
  }

  private void decWeight(Record record, int decWeight) {
    LoadBalanceStats.instance().get(record.getRegistration())
            .decWeight(decWeight);
  }

  private void incWeight(Record record, int incWeight) {
    LoadBalanceStats.instance().get(record.getRegistration())
            .incWeight(incWeight);
  }

}
