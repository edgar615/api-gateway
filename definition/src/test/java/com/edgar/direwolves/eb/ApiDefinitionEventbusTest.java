package com.edgar.direwolves.eb;

import static org.awaitility.Awaitility.await;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.direwolves.verticle.ApiDefinitionVerticle;
import com.edgar.util.base.Randoms;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edgar on 2016/9/13.
 *
 * @author Edgar  Date 2016/9/13
 */
@RunWith(VertxUnitRunner.class)
public class ApiDefinitionEventbusTest {

  Vertx vertx;

  EventBus eb;

  private String deployId;

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();
    eb = vertx.eventBus();
    vertx.deployVerticle(ApiDefinitionVerticle.class.getName(), context.asyncAssertSuccess());
  }

  @After
  public void clear(TestContext context) {
//    vertx.close(context.asyncAssertSuccess());
    ApiDefinitionRegistry.create().remove(null);
  }

  @Test
  public void testListAll(TestContext context) {
    add(context);
    add("src/test/resources/device_update.json", context);

    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);

    JsonObject queryJson = new JsonObject();
    eb.<List<ApiDefinition>>send(ApiListHandler.ADDRESS, queryJson, ar -> {
      if (ar.succeeded()) {
        List<ApiDefinition> definitions = ar.result().body();
        System.out.println(definitions);

        context.assertEquals(2, definitions.size());
        ApiDefinition apiDefinition = definitions.get(0);
        context.assertEquals("add_device", apiDefinition.name());
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });
  }

  @Test
  public void testListAll2(TestContext context) {
    add(context);
    add("src/test/resources/device_update.json", context);

    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);

    JsonObject queryJson = new JsonObject().put("name", "*");
    eb.<List<ApiDefinition>>send(ApiListHandler.ADDRESS, queryJson, ar -> {
      if (ar.succeeded()) {
        List<ApiDefinition> definitions = ar.result().body();
        System.out.println(definitions);

        context.assertEquals(2, definitions.size());
        ApiDefinition apiDefinition = definitions.get(0);
        context.assertEquals("add_device", apiDefinition.name());
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });

  }

  @Test
  public void testListEmpty(TestContext context) {
    add(context);
    add("src/test/resources/device_update.json", context);

    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);

    JsonObject queryJson = new JsonObject().put("name", Randoms.randomAlphabet(10));
    eb.<List<ApiDefinition>>send(ApiListHandler.ADDRESS, queryJson, ar -> {
      if (ar.succeeded()) {
        List<ApiDefinition> definitions = ar.result().body();
        System.out.println(definitions);

        context.assertEquals(0, definitions.size());
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });

  }

  @Test
  public void testListStart(TestContext context) {
    add(context);
    add("src/test/resources/device_update.json", context);

    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);

    JsonObject queryJson = new JsonObject().put("name", "*")
            .put("start", 1);
    eb.<List<ApiDefinition>>send(ApiListHandler.ADDRESS, queryJson, ar -> {
      if (ar.succeeded()) {
        List<ApiDefinition> definitions = ar.result().body();
        System.out.println(definitions);

        context.assertEquals(1, definitions.size());
        ApiDefinition apiDefinition = definitions.get(0);
        context.assertEquals("update_device", apiDefinition.name());
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });

  }

  @Test
  public void testListLimit(TestContext context) {
    add(context);
    add("src/test/resources/device_update.json", context);

    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);

    JsonObject queryJson = new JsonObject().put("name", "*")
            .put("limit", 1);
    eb.<List<ApiDefinition>>send(ApiListHandler.ADDRESS, queryJson, ar -> {
      if (ar.succeeded()) {
        List<ApiDefinition> definitions = ar.result().body();
        System.out.println(definitions);

        context.assertEquals(1, definitions.size());
        ApiDefinition apiDefinition = definitions.get(0);
        context.assertEquals("add_device", apiDefinition.name());
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });

  }

  @Test
  public void testMatch(TestContext context) {
    add(context);
    List<ApiDefinition> copyApiDefinition = new ArrayList<>();
    eb.<List<ApiDefinition>>send(ApiMatchHandler.ADDRESS, new JsonObject()
            .put("method", "POST")
            .put("path", "/devices"), ar -> {
      if (ar.succeeded()) {
        List<ApiDefinition> definitions = ar.result().body();
        context.assertEquals(1, definitions.size());
        copyApiDefinition.addAll(definitions);
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });
    await().until(() -> copyApiDefinition.size() == 1);

    copyApiDefinition.clear();
    eb.<List<ApiDefinition>>send(ApiMatchHandler.ADDRESS, new JsonObject()
            .put("method", "GET")
            .put("path", "/devices"), ar -> {
      if (ar.succeeded()) {
        List<ApiDefinition> definitions = ar.result().body();
        context.assertEquals(0, definitions.size());
        copyApiDefinition.addAll(definitions);
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });
    await().until(() -> copyApiDefinition.size() == 0);

    copyApiDefinition.clear();
    eb.<List<ApiDefinition>>send(ApiMatchHandler.ADDRESS, new JsonObject()
            .put("method", "POST")
            .put("path", "/devices/1"), ar -> {
      if (ar.succeeded()) {
        List<ApiDefinition> definitions = ar.result().body();
        context.assertEquals(0, definitions.size());
        copyApiDefinition.addAll(definitions);
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });
    await().until(() -> copyApiDefinition.size() == 0);
  }

  private void add(String file, TestContext context) {
    JsonObject
            addDeviceJson = new JsonObject();// JsonUtils.getJsonFromFile(file);
  }

  private void add(TestContext context) {
    add("src/test/resources/device_add.json", context);
  }
}
