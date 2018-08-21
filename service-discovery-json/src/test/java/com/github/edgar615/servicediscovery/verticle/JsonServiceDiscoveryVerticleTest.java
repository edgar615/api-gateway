package com.github.edgar615.servicediscovery.verticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/11/9.
 *
 * @author Edgar  Date 2017/11/9
 */
@RunWith(VertxUnitRunner.class)
public class JsonServiceDiscoveryVerticleTest {

    private Vertx vertx;

    private ServiceDiscovery serviceDiscovery;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        serviceDiscovery = ServiceDiscovery.create(vertx);
    }

    @Test
    public void testPublish(TestContext testContext) {
        JsonObject services = new JsonObject()
                .put("user", new JsonArray()
                        .add(new JsonObject().put("host", "192.168.0.100").put("port", 8080))
                        .add(new JsonObject().put("host", "192.168.0.101").put("port", 8080)))
                .put("device", new JsonArray()
                        .add(new JsonObject().put("host", "192.168.0.100").put("port", 8081)));
        JsonObject config = new JsonObject()
                .put("services", services);
        vertx.deployVerticle(JsonServiceDiscoveryVerticle.class.getName(), new DeploymentOptions()
                .setConfig(config));
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AtomicBoolean check = new AtomicBoolean();
        serviceDiscovery.getRecords(r -> true, ar -> {
            if (ar.succeeded()) {
                testContext.assertEquals(3, ar.result().size());
                check.set(true);
            } else {
                testContext.fail();
            }
        });
        Awaitility.await().until(() -> check.get());

        AtomicBoolean check2 = new AtomicBoolean();
        serviceDiscovery.getRecords(new JsonObject().put("name", "device"), ar -> {
            if (ar.succeeded()) {
                testContext.assertEquals(1, ar.result().size());
                check2.set(true);
            } else {
                testContext.fail();
            }
        });
        Awaitility.await().until(() -> check2.get());
    }
}
