package com.github.edgar615.gateway.core.apidiscovery;

import com.google.common.collect.Lists;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2016/4/11.
 *
 * @author Edgar  Date 2016/4/11
 */
@RunWith(VertxUnitRunner.class)
public class ApiDiscoveryTest {

    Vertx vertx;

    ApiDiscovery discovery;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        discovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions());
    }

    @After
    public void clear() {
        AtomicBoolean completed = new AtomicBoolean();
        discovery.clear(ar -> completed.set(true));
        Awaitility.await().until(() -> completed.get());

    }

    @Test
    public void testRegister(TestContext testContext) {
        AtomicBoolean check1 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject(), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(0, ar.result().size());
            check1.set(true);
        });
        Awaitility.await().until(() -> check1.get());

        SimpleHttpEndpoint httpEndpoint = SimpleHttpEndpoint
                .http("get_device", HttpMethod.GET, "devices/",
                      80, "localhost");

        ApiDefinition apiDefinition = ApiDefinition
                .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));
        AtomicBoolean completed = new AtomicBoolean();
        discovery.publish(apiDefinition, ar -> completed.set(true));
        Awaitility.await().until(() -> completed.get());

        AtomicBoolean check2 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject(), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(1, ar.result().size());
            check2.set(true);
        });

        Awaitility.await().until(() -> check2.get());

    }

    @Test
    public void testUniqueName(TestContext testContext) {
        SimpleHttpEndpoint httpEndpoint = SimpleHttpEndpoint
                .http("get_device", HttpMethod.GET, "devices/",
                      80, "localhost");

        ApiDefinition apiDefinition = ApiDefinition
                .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

        AtomicBoolean completed = new AtomicBoolean();
        discovery.publish(apiDefinition, ar -> completed.set(true));
        Awaitility.await().until(() -> completed.get());

        AtomicBoolean check1 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject(), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(1, ar.result().size());
            check1.set(true);
        });
        Awaitility.await().until(() -> check1.get());

        AtomicBoolean completed2 = new AtomicBoolean();
        discovery.publish(apiDefinition, ar -> completed2.set(true));
        Awaitility.await().until(() -> completed2.get());

        AtomicBoolean check2 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject(), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(1, ar.result().size());
            check2.set(true);
        });

        Awaitility.await().until(() -> check2.get());
    }

    @Test
    public void testFilterByName(TestContext testContext) {
        SimpleHttpEndpoint httpEndpoint = SimpleHttpEndpoint
                .http("get_device", HttpMethod.GET, "devices/",
                      80, "localhost");

        ApiDefinition apiDefinition = ApiDefinition
                .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));
        AtomicBoolean completed = new AtomicBoolean();
        discovery.publish(apiDefinition, ar -> completed.set(true));
        Awaitility.await().until(() -> completed.get());

        apiDefinition = ApiDefinition
                .create("get_device2", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));
        AtomicBoolean completed2 = new AtomicBoolean();
        discovery.publish(apiDefinition, ar -> completed2.set(true));
        Awaitility.await().until(() -> completed2.get());

        AtomicBoolean check1 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject(), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(2, ar.result().size());
            check1.set(true);
        });
        Awaitility.await().until(() -> check1.get());

        AtomicBoolean check2 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject().put("name", "get_device"), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(1, ar.result().size());
            testContext.assertEquals("get_device", ar.result().get(0).name());
            check2.set(true);
        });
        Awaitility.await().until(() -> check2.get());

        AtomicBoolean check3 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject().put("name", "get_device3"), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(0, ar.result().size());
            check3.set(true);
        });
        Awaitility.await().until(() -> check3.get());

        AtomicBoolean check4 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject().put("name", "get*"), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(2, ar.result().size());
            check4.set(true);
        });
        Awaitility.await().until(() -> check4.get());

        AtomicBoolean check5 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject().put("name", "*device*"), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(0, ar.result().size());
            check5.set(true);
        });
        Awaitility.await().until(() -> check5.get());

        AtomicBoolean check6 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject().put("name", "***"), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(0, ar.result().size());
            check6.set(true);
        });
        Awaitility.await().until(() -> check6.get());

        AtomicBoolean check7 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject().put("name", "*"), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(2, ar.result().size());
            check7.set(true);
        });
        Awaitility.await().until(() -> check7.get());

        AtomicBoolean check8 = new AtomicBoolean();
        discovery.getDefinitions(new JsonObject().put("name", "***"), ar -> {
            if (ar.failed()) {
                testContext.fail();
                return;
            }
            testContext.assertEquals(0, ar.result().size());
            check8.set(true);
        });
        Awaitility.await().until(() -> check8.get());
    }

    @Test
    public void testFilter(TestContext testContext) {
        SimpleHttpEndpoint httpEndpoint = SimpleHttpEndpoint
                .http("get_device", HttpMethod.GET, "devices/",
                      80, "localhost");

        ApiDefinition apiDefinition = ApiDefinition
                .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

        AtomicBoolean completed = new AtomicBoolean();
        discovery.publish(apiDefinition, ar -> completed.set(true));
        Awaitility.await().until(() -> completed.get());

        AtomicBoolean check2 = new AtomicBoolean();
        discovery.filter("get", "/device", ar -> {
            testContext.assertEquals(1, ar.result().size());
            check2.set(true);
        });
        Awaitility.await().until(() -> check2.get());

        AtomicBoolean check3 = new AtomicBoolean();
        discovery.filter("get", "/devices", ar -> {
            testContext.assertEquals(0, ar.result().size());
            check3.set(true);
        });
        Awaitility.await().until(() -> check3.get());
    }

}
