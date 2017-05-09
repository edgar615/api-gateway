package com.edgar.direwolves.discovery;

import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edgar on 17-5-6.
 */
public class StrategyTest {
  List<Record> instances = new ArrayList<>();

  @Before
  public void setUp() {
    instances.clear();
    instances.add(HttpEndpoint.createRecord("device", "localhost", 8080, "/").setRegistration("a"));
    instances.add(HttpEndpoint.createRecord("device", "localhost", 8080, "/").setRegistration("b"));
    instances.add(HttpEndpoint.createRecord("device", "localhost", 8080, "/").setRegistration("c"));
  }

  protected List<String> select100(SelectStrategy strategy) {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 3000; i++) {
      Record record = strategy.get(instances);
      selected.add(record.getRegistration());
    }
    return selected;
  }
}
