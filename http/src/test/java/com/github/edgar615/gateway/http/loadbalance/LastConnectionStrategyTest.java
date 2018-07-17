package com.github.edgar615.gateway.http.loadbalance;

import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edgar on 17-5-6.
 */
public class LastConnectionStrategyTest {
  List<Record> instances = new ArrayList<>();

  @Before
  public void setUp() {
    instances.clear();
    instances.add(HttpEndpoint.createRecord("test", "localhost", 8081, "/").setRegistration("a"));
    instances.add(HttpEndpoint.createRecord("test", "localhost", 8082, "/").setRegistration("b"));
    instances.add(HttpEndpoint.createRecord("test", "localhost", 8083, "/").setRegistration("c"));
    LoadBalanceStats.instance().get("a").incActiveRequests().incActiveRequests().decActiveRequests();
    LoadBalanceStats.instance().get("b").incActiveRequests().decActiveRequests().decActiveRequests();
    LoadBalanceStats.instance().get("c").incActiveRequests().incActiveRequests().incActiveRequests();
  }
  @Test
  public void testLastConn() {
    ChooseStrategy chooseStrategy = ChooseStrategy.lastConnection();
    Record instance = chooseStrategy.get(instances);
    Assert.assertEquals(instance.getRegistration(), "b");
  }

}
