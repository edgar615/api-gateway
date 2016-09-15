package com.edgar.direwolves.definition;

import com.edgar.util.base.Randoms;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.awaitility.Awaitility.await;

/**
 * Created by Edgar on 2016/9/13.
 *
 * @author Edgar  Date 2016/9/13
 */
@RunWith(VertxUnitRunner.class)
public class ApiDefinitionVerticleTest {

    Vertx vertx;

    EventBus eb;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        eb = vertx.eventBus();
        vertx.deployVerticle(ApiDefinitionVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public void clear(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
        ApiDefinitionRegistryImpl.instance().remove(null);
        AuthDefinitionRegistry.create().remove(null, null);
        IpRestrictionDefinitionRegistry.create().remove(null);
        RateLimitDefinitionRegistry.create().remove(null, null, null);
    }

    @Test
    public void testAddSuccess(TestContext context) {
        JsonObject addDeviceJson =  JsonUtils.getJsonFromFile("src/test/resources/device_add.json");

//        Async async = context.async();
        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, addDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
//            async.complete();
        });

        await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
        await().until(() -> AuthDefinitionRegistry.create().filter(null, null).size() > 0);
        await().until(() -> RateLimitDefinitionRegistry.create().filter(null, null, null).size() > 0);
        await().until(() -> IpRestrictionDefinitionRegistry.create().filter(null).size() > 0);
    }

    @Test
    public void testAddSuccess2(TestContext context) {
        JsonObject addDeviceJson =  JsonUtils.getJsonFromFile("src/test/resources/device_add2.json");

//        Async async = context.async();
        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, addDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
//            async.complete();
        });

        await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
        await().until(() -> AuthDefinitionRegistry.create().filter(null, null).size() == 0);
        await().until(() -> RateLimitDefinitionRegistry.create().filter(null, null, null).size() == 0);
        await().until(() -> IpRestrictionDefinitionRegistry.create().filter(null).size() == 0);
    }

    @Test
    public void testAddSameName(TestContext context) {
        JsonObject addDeviceJson =  JsonUtils.getJsonFromFile("src/test/resources/device_add.json");

        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, addDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });

        await().until(() -> ApiDefinitionRegistryImpl.instance().filter(null).size() > 0);
        await().until(() -> ApiDefinitionRegistryImpl.instance().filter("add_device").get(0).method().equals(HttpMethod.POST));

        addDeviceJson =  JsonUtils.getJsonFromFile("src/test/resources/device_add2.json");

        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, addDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });
        await().until(() -> ApiDefinitionRegistryImpl.instance().filter(null).size() > 0);
        await().until(() -> ApiDefinitionRegistryImpl.instance().filter("add_device").get(0).method().equals(HttpMethod.PUT));
    }

    @Test
    public void testList(TestContext context) {
        JsonObject addDeviceJson =  JsonUtils.getJsonFromFile("src/test/resources/device_add.json");

        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, addDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });

        await().until(() -> ApiDefinitionRegistryImpl.instance().filter(null).size() > 0);

        JsonObject queryJson = new JsonObject();
        eb.<JsonArray>send(ApiDefinitionVerticle.API_LIST, queryJson, ar -> {
            if (ar.succeeded()) {
                JsonArray jsonArray = ar.result().body();
                System.out.println(jsonArray);

                context.assertEquals(1, jsonArray.size());
                JsonObject jsonObject = jsonArray.getJsonObject(0);
                context.assertEquals("add_device", jsonObject.getString("name"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });
    }

    @Test
    public void testGet(TestContext context) {
        JsonObject addDeviceJson =  JsonUtils.getJsonFromFile("src/test/resources/device_add.json");

        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, addDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });

        await().until(() -> ApiDefinitionRegistryImpl.instance().filter(null).size() > 0);

        Async async = context.async();

        JsonObject queryJson = new JsonObject()
                .put("name", "add_device");
        eb.<JsonObject>send(ApiDefinitionVerticle.API_GET, queryJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                context.assertEquals("add_device", jsonObject.getString("name"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
            async.complete();
        });

        Async async2 = context.async();

        queryJson = new JsonObject()
                .put("name", "*device");
        eb.<JsonObject>send(ApiDefinitionVerticle.API_GET, queryJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                context.assertEquals("add_device", jsonObject.getString("name"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
            async2.complete();
        });
    }

    @Test
    public void testGet404(TestContext context) {
        JsonObject addDeviceJson =  JsonUtils.getJsonFromFile("src/test/resources/device_add.json");

        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, addDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });

        await().until(() -> ApiDefinitionRegistryImpl.instance().filter(null).size() > 0);

        Async async = context.async();

        JsonObject queryJson = new JsonObject()
                .put("name", Randoms.randomAlphabet(10));
        eb.<JsonObject>send(ApiDefinitionVerticle.API_GET, queryJson, ar -> {
            if (ar.succeeded()) {
                context.fail();
            } else {
                System.out.println(ar.cause());
            }
            async.complete();
        });
    }

    @Test
    public void testDelete(TestContext context) {
        JsonObject addDeviceJson =  JsonUtils.getJsonFromFile("src/test/resources/device_add.json");

        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, addDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });


        JsonObject queryJson = new JsonObject()
                .put("name", "*device");
        eb.<JsonObject>send(ApiDefinitionVerticle.API_DELETE, queryJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                context.assertEquals("OK", jsonObject.getString("result"));
                context.assertEquals(0, ApiDefinitionRegistryImpl.instance().getDefinitions().size());
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });
        await().until(() -> ApiDefinitionRegistryImpl.instance().filter(null).size() == 0);
    }
}
