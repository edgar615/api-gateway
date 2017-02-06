package com.edgar.direwolves.servicediscovery.zookeeper;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Created by Edgar on 2017/2/6.
 *
 * @author Edgar  Date 2017/2/6
 */
@RunWith(VertxUnitRunner.class)
public class ZookeeperServiceImporterTest {

  Vertx vertx;
  ServiceDiscovery discovery;

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
    JsonObject config = new JsonObject()
            .put("zookeeper.connect", "10.11.0.31:2181")
            .put("zookeeper.path", "/csst-microservice");
    discovery = ServiceDiscovery.create(vertx);
    discovery.registerServiceImporter(new ZookeeperServiceImporter(), config,
                                      testContext.asyncAssertSuccess());
  }

  @Test
  public  void testList(TestContext testContext) {
    Async async = testContext.async();
    discovery.getRecords(r -> true, ar -> {
      if (ar.succeeded()) {
        System.out.println(ar.result());
        List<Record> records = ar.result();
        records.forEach(r -> System.out.println(r.getLocation()));
        records.forEach(r -> System.out.println(r.getMetadata()));
        records.forEach(r -> System.out.println(r.getType()));
        async.complete();
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
  }
}
