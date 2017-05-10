package com.edgar.direwolves.discovery;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by Edgar on 2017/5/10.
 *
 * @author Edgar  Date 2017/5/10
 */
@RunWith(VertxUnitRunner.class)
public class ServiceDiscoveryTest {

  private Vertx vertx;

  private io.vertx.servicediscovery.ServiceDiscovery discovery;

  private ServiceDiscovery provider;

  private String aId;

  private String bId;

  private String cId;

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
    discovery = io.vertx.servicediscovery.ServiceDiscovery.create(vertx);
    provider = ServiceDiscovery.create(vertx, new JsonObject());

    aId = UUID.randomUUID().toString();
    bId = UUID.randomUUID().toString();
    cId = UUID.randomUUID().toString();
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8080, "/").setMetadata(new
                                                                                                    JsonObject()
                                                                                                    .put("ID",
                                                                                                         aId)),
                      ar -> {
                        if (ar.failed()) {
                          ar.cause().printStackTrace();
                        } else {
                          System.out.println(ar.result().getRegistration());
                        }
                      });
    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8081, "/").setMetadata(new
                                                                                                    JsonObject()
                                                                                                    .put("ID",
                                                                                                         bId)),
                      ar -> {
                        if (ar.failed()) {
                          ar.cause().printStackTrace();
                        } else {
                          System.out.println(ar.result().getRegistration());
                        }
                      });

    discovery.publish(HttpEndpoint.createRecord("test", "localhost", 8082, "/").setMetadata(new
                                                                                                    JsonObject()
                                                                                                    .put("ID",
                                                                                                         cId)),
                      ar -> {
                        if (ar.failed()) {
                          ar.cause().printStackTrace();
                        } else {
                          System.out.println(ar.result().getRegistration());
                        }
                      });
  }

  @Test
  public void testDefault() {
    List<String> selected = select3000();
    Assert.assertEquals(3, new HashSet<>(selected).size());
    long aSize = selected.stream()
            .filter(i -> "8080".equals(i))
            .count();
    long bSize = selected.stream()
            .filter(i -> "8081".equals(i))
            .count();
    long cSize = selected.stream()
            .filter(i -> "8082".equals(i))
            .count();
    Assert.assertEquals(aSize, 1000);
    Assert.assertEquals(bSize, 1000);
    Assert.assertEquals(cSize, 1000);
  }

  @Test
  public void testRandom() {
    List<String> selected = select3000();
    Assert.assertEquals(3, new HashSet<>(selected).size());
    long aSize = selected.stream()
            .filter(i -> "8080".equals(i))
            .count();
    long bSize = selected.stream()
            .filter(i -> "8081".equals(i))
            .count();
    long cSize = selected.stream()
            .filter(i -> "8082".equals(i))
            .count();
    Assert.assertNotEquals(aSize, 1000);
    Assert.assertNotEquals(bSize, 1000);
    Assert.assertNotEquals(cSize, 1000);
  }

  private List<String> select3000() {
    List<String> selected = new ArrayList<>();
    for (int i = 0; i < 3000; i++) {
      ServiceInstance instance = provider.getInstance("test");
      selected.add(instance.record().getLocation().getInteger("port") + "");
    }
    return selected;
  }
}
