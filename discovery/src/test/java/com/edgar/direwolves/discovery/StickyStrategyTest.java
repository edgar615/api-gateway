package com.edgar.direwolves.discovery;

import io.vertx.servicediscovery.Record;
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
    SelectStrategy selectStrategy = SelectStrategy.sticky(SelectStrategy.roundRobin());
    List<String> selected = select100(selectStrategy);
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
    SelectStrategy selectStrategy = SelectStrategy.sticky(SelectStrategy.roundRobin());
    List<String> selected = select(selectStrategy);
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

  protected List<String> select(SelectStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      Record instance = strategy.get(instances);
      selected.add(instance.getRegistration());
    }

    instances.removeIf(r -> r.getRegistration().equalsIgnoreCase("a"));
    for (int i = 1000; i < 2000; i++) {
      Record instance = strategy.get(instances);
      selected.add(instance.getRegistration());
    }

    instances.add(HttpEndpoint.createRecord("device", "localhost", 8080, "/").setRegistration("a"));
    for (int i = 2000; i < 3000; i++) {
      Record instance = strategy.get(instances);
      selected.add(instance.getRegistration());
    }
    return selected;
  }

}
