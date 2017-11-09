package com.github.edgar615.servicediscovery.verticle;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2017/11/9.
 *
 * @author Edgar  Date 2017/11/9
 */
@RunWith(VertxUnitRunner.class)
public class ZookeeperServiceDiscoveryVerticleTest {

  private Vertx vertx;

  private ServiceDiscovery serviceDiscovery;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    serviceDiscovery = ServiceDiscovery.create(vertx);
  }

  /**
   * zookeeper需要集成
   *
   * @param testContext
   */
  @Test
  public void testPublish(TestContext testContext) {
//    JsonObject zookeeper = new JsonObject()
//            .put("connect", "127.0.0.1");
//    JsonObject config = new JsonObject()
//            .put("services", zookeeper);
//    vertx.deployVerticle(ZookeeperServiceDiscoveryVerticle.class.getName(), new
// DeploymentOptions()
//            .setConfig(config));
//    try {
//      TimeUnit.SECONDS.sleep(3);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//    AtomicBoolean check = new AtomicBoolean();
//    serviceDiscovery.getRecords(r -> true, ar -> {
//      if (ar.succeeded()) {
//        testContext.assertEquals(3, ar.result().size());
//        check.set(true);
//      } else {
//        testContext.fail();
//      }
//    });
//    Awaitility.await().until(() -> check.get());
//
//    AtomicBoolean check2 = new AtomicBoolean();
//    serviceDiscovery.getRecords(new JsonObject().put("name", "device"), ar -> {
//      if (ar.succeeded()) {
//        testContext.assertEquals(1, ar.result().size());
//        check2.set(true);
//      } else {
//        testContext.fail();
//      }
//    });
//    Awaitility.await().until(() -> check2.get());
  }
}
