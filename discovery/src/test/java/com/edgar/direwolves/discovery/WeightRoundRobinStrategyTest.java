package com.edgar.direwolves.discovery;

import io.vertx.core.json.JsonObject;
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
    instances.add(HttpEndpoint.createRecord("device", "localhost", 8080, "/")
                          .setRegistration
                                  ("a").setMetadata(new JsonObject().put("weight", 5)));
    instances.add(HttpEndpoint.createRecord("device", "localhost", 8080, "/")
                          .setRegistration
                                  ("b").setMetadata(new JsonObject().put("weight", 1)));
    instances.add(HttpEndpoint.createRecord("device", "localhost", 8080, "/")
                          .setRegistration
                                  ("c").setMetadata(new JsonObject().put("weight", 1)));
  }


  @Test
  public void testWeight() {
    SelectStrategy selectStrategy = SelectStrategy.weightRoundRobin();
    List<String> selected = select(selectStrategy);
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
    SelectStrategy selectStrategy = SelectStrategy.weightRoundRobin();
    List<String> selected = select7(selectStrategy);
    Assert.assertEquals(3, new HashSet<>(selected).size());
    System.out.println(selected);
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

  private List<String> select7(SelectStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      Record instance = strategy.get(instances);
      selected.add(instance.getRegistration());
    }
    return selected;
  }

  private List<String> select(SelectStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 7000; i++) {
      Record instance = strategy.get(instances);
      selected.add(instance.getRegistration());
    }
    return selected;
  }
}
