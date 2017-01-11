package com.edgar.direwolves.record;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.record.SelectStrategy;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Edgar on 2016/10/12.
 *
 * @author Edgar  Date 2016/10/12
 */
public class SelectStrategyTest {

  private List<Record> records = new ArrayList<>();

  @Before
  public void setup() {
    records.clear();
    records.add(HttpEndpoint.createRecord("device", "localhost", 8080, "/"));
    records.add(HttpEndpoint.createRecord("device", "localhost", 8081, "/"));
  }

  @After
  public void tearDown() {
    records.clear();
  }

  @Test
  public void testRoundRobin() {
    SelectStrategy selectStrategy = SelectStrategy.create("round_robin");
    Multimap<Integer, Record> group = select100(selectStrategy);
    Assert.assertEquals(2, group.keySet().size());
    Assert.assertEquals(50, group.get(8080).size());
    Assert.assertEquals(50, group.get(8081).size());
  }

  @Test
  public void testRandom() {
    SelectStrategy selectStrategy = SelectStrategy.create("random");
    Multimap<Integer, Record> group = select100(selectStrategy);
    Assert.assertEquals(2, group.keySet().size());
    Assert.assertFalse(group.get(8080).size() == group.get(8081).size());
  }

  @Test
  public void testNull() {
    SelectStrategy selectStrategy = SelectStrategy.create(UUID.randomUUID().toString());
    Assert.assertNull(selectStrategy);
  }

  private Multimap<Integer, Record> select100(SelectStrategy selectStrategy) {
    Multimap<Integer, Record> group = ArrayListMultimap.create();
    for (int i = 0; i < 100; i++) {
      Record record = selectStrategy.select(records);
      int port = record.getLocation().getInteger("port");
      group.put(port, record);
    }
    return group;
  }
}
