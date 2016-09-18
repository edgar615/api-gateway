package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.definition.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Optional;

import static org.awaitility.Awaitility.await;

/**
 * Created by edgar on 16-9-12.
 */
@RunWith(VertxUnitRunner.class)
public class ApiMatcherTest {

    ApiDefinitionRegistry registry;

    Vertx vertx;

    EventBus eb;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        eb = vertx.eventBus();
        registry = ApiDefinitionRegistry.create();
        vertx.deployVerticle(ApiDefinitionVerticle.class.getName(), context.asyncAssertSuccess());

        addDeviceJson(context);

        getDeviceJson(context);

        deleteDeviceJson(context);

        updateDeviceJson(context);

        listDeviceJson(context);

        getPartJson(context);

        await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 6);
    }

    private void getPartJson(TestContext context) {
        JsonObject getPartJson = JsonUtils.getJsonFromFile("src/test/resources/part_get.json");

        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, getPartJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });
    }

    private void listDeviceJson(TestContext context) {
        JsonObject listDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_list.json");

        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, listDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });
    }

    private void updateDeviceJson(TestContext context) {
        JsonObject updateDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_update.json");

        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, updateDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });
    }

    private void deleteDeviceJson(TestContext context) {
        JsonObject deleteDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_delete.json");

        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, deleteDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });
    }

    private void getDeviceJson(TestContext context) {
        JsonObject getDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_get.json");

        eb.<JsonObject>send(ApiDefinitionVerticle.API_ADD, getDeviceJson, ar -> {
            if (ar.succeeded()) {
                JsonObject jsonObject = ar.result().body();
                System.out.println(jsonObject);
                context.assertEquals("OK", jsonObject.getString("result"));
            } else {
                System.out.println(ar.cause());
                context.fail();
            }
        });
    }

    private void addDeviceJson(TestContext context) {
        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");

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
    }

    @After
    public void clear(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
        ApiDefinitionRegistry.create().remove(null);
        AuthDefinitionRegistry.create().remove(null, null);
        IpRestrictionDefinitionRegistry.create().remove(null);
        RateLimitDefinitionRegistry.create().remove(null, null, null);
    }


    @Test
    public void testMatch() {
        ApiContext apiContext = ApiContext.builder().setPath("/devices")
                .setMethod(HttpMethod.POST)
                .setBody(new JsonObject())
                .build();
        Optional<ApiDefinition> optional =
                registry.getDefinitions().stream().filter(definition -> ApiMatcher.instance().apply(apiContext, definition))
                        .findFirst();
        Assert.assertTrue(optional.isPresent());
    }

    @Test
    public void testNotMatch() {
        ApiContext apiContext = ApiContext.builder().setPath("/devices")
                .setMethod(HttpMethod.PUT)
                .setBody(new JsonObject())
                .build();
        Optional<ApiDefinition> optional =
                registry.getDefinitions().stream().filter(definition -> ApiMatcher.instance().apply(apiContext, definition))
                        .findFirst();
        Assert.assertFalse(optional.isPresent());
    }

    @Test
    public void testOneParam() {
        ApiContext apiContext = ApiContext.builder().setPath("/devices/1")
                .setMethod(HttpMethod.GET)
                .setBody(new JsonObject())
                .setParams(ArrayListMultimap.create())
                .build();
        Optional<ApiDefinition> optional =
                registry.getDefinitions().stream().filter(definition -> ApiMatcher.instance().apply(apiContext, definition))
                        .findFirst();
        Assert.assertTrue(optional.isPresent());
        Assert.assertTrue(apiContext.params().containsKey("param1"));
        Assert.assertEquals("1", Iterables.get(apiContext.params().get("param1"), 0));
    }

    @Test
    public void testTwoParam() {
        ApiContext apiContext = ApiContext.builder().setPath("/devices/1/parts/2")
                .setMethod(HttpMethod.GET)
                .setBody(new JsonObject())
                .setParams(ArrayListMultimap.create())
                .build();
        Optional<ApiDefinition> optional =
                registry.getDefinitions().stream().filter(definition -> ApiMatcher.instance().apply(apiContext, definition))
                        .findFirst();
        Assert.assertTrue(optional.isPresent());
        Assert.assertTrue(apiContext.params().containsKey("param1"));
        Assert.assertEquals("1", Iterables.get(apiContext.params().get("param1"), 0));
        Assert.assertEquals("2", Iterables.get(apiContext.params().get("param2"), 0));
    }

    @Test
    public void testUnMatcher() {
        ApiContext apiContext = ApiContext.builder().setPath("/devices/abc")
                .setMethod(HttpMethod.GET)
                .setBody(new JsonObject())
                .setParams(ArrayListMultimap.create())
                .build();
        Optional<ApiDefinition> optional =
                registry.getDefinitions().stream().filter(definition -> ApiMatcher.instance().apply(apiContext, definition))
                        .findFirst();
        Assert.assertFalse(optional.isPresent());
    }
}
