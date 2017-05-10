package com.edgar.direwolves.discovery;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2017/5/10.
 *
 * @author Edgar  Date 2017/5/10
 */
@RunWith(VertxUnitRunner.class)
public class ServiceProviderTest {

  private Vertx vertx;

  private ServiceDiscovery discovery;

  private ServiceProvider provider;

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
    discovery = ServiceDiscovery.create(vertx);
    provider = ServiceProvider.create(vertx, new JsonObject());
  }

  @Test
  public void testRandom() {
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8080, "/"), ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        System.out.println(ar.result().getRegistration());
      }
    });
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8081, "/"), ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        System.out.println(ar.result().getRegistration());
      }
    });

    System.out.println(provider.getInstance("test"));
  }
}
