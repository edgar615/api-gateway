package com.edgar.direwolves.eb;

import static org.awaitility.Awaitility.await;

import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.core.spi.ApiDefinition;
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
  public void testAddSuccess(TestContext context) {
    add(context);
    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
    Assert.assertEquals(1, ApiDefinitionRegistry.create().filter("add_device").size());

    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
    Assert.assertEquals("/devices", apiDefinition.path());
    Assert.assertEquals("device:write", apiDefinition.scope());

    Assert.assertEquals(6, apiDefinition.plugins().size());
//    Assert.assertNull(apiDefinition.plugin(AclRestriction.NAME));
//    Assert.assertNotNull(apiDefinition.plugin(IpRestriction.NAME));
//    Assert.assertNotNull(apiDefinition.plugin(UrlArgPlugin.NAME));
//    Assert.assertNotNull(apiDefinition.plugin(BodyArgPlugin.NAME));
//    Assert.assertNotNull(apiDefinition.plugin(RateLimitPlugin.NAME));
//    Assert.assertNotNull(apiDefinition.plugin(RequestTransformerPlugin.NAME));
//    Assert.assertNotNull(apiDefinition.plugin(ResponseTransformerPlugin.NAME));

  }

  @Test
  public void testAddSameName(TestContext context) {
    add("src/test/resources/device_add.json", context);

    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
    await().until(() -> ApiDefinitionRegistry.create().filter("add_device").get(0)
            .method().equals(HttpMethod.POST));

    Assert.assertEquals(1, ApiDefinitionRegistry.create().filter(null).size());
    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
    Assert.assertEquals(6, apiDefinition.plugins().size());
    Assert.assertEquals(1, apiDefinition.endpoints().size());

    add("src/test/resources/device_add2.json", context);
    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
    await().until(() -> ApiDefinitionRegistry.create().filter("add_device").get(0)
            .method().equals(HttpMethod.PUT));

    Assert.assertEquals(1, ApiDefinitionRegistry.create().filter(null).size());
    apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
    Assert.assertEquals(4, apiDefinition.plugins().size());
    Assert.assertEquals(2, apiDefinition.endpoints().size());
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
  public void testGet(TestContext context) {
    add(context);

    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);

    Async async = context.async();

    eb.<ApiDefinition>send(ApiGetHandler.ADDRESS, "add_device", ar -> {
      if (ar.succeeded()) {
        ApiDefinition apiDefinition = ar.result().body();
        context.assertEquals("add_device", apiDefinition.name());
      } else {
        ar.cause().printStackTrace();
        System.out.println(ar.cause());
        context.fail();
      }
      async.complete();
    });

    Async async2 = context.async();

    eb.<ApiDefinition>send(ApiGetHandler.ADDRESS, "*device", ar -> {
      if (ar.succeeded()) {
        ApiDefinition apiDefinition = ar.result().body();
        context.assertEquals("add_device", apiDefinition.name());
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
      async2.complete();
    });
  }

  @Test
  public void testGet404(TestContext context) {
    add(context);

    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);

    Async async = context.async();

    eb.<JsonObject>send(ApiGetHandler.ADDRESS, Randoms.randomAlphabet(10), ar -> {
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
    add(context);
    eb.<JsonObject>send(ApiDeleteHandler.ADDRESS, "*device", ar -> {
      if (ar.succeeded()) {
        JsonObject jsonObject = ar.result().body();
        context.assertEquals("OK", jsonObject.getString("result"));
        context.assertEquals(0, ApiDefinitionRegistry.create().getDefinitions()
                .size());
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });
    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 0);
  }

  @Test
  public void testDeleteAll(TestContext context) {
    add(context);
    eb.<JsonObject>send(ApiDeleteHandler.ADDRESS, "*", ar -> {
      if (ar.succeeded()) {
        JsonObject jsonObject = ar.result().body();
        context.assertEquals("OK", jsonObject.getString("result"));
        context.assertEquals(0, ApiDefinitionRegistry.create().getDefinitions()
                .size());
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });
    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 0);
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
            addDeviceJson = JsonUtils.getJsonFromFile(file);
    eb.<JsonObject>send(ApiAddHandler.ADDRESS, addDeviceJson, ar -> {
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

  private void add(TestContext context) {
    add("src/test/resources/device_add.json", context);
  }
}
