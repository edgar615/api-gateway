package com.edgar.direwolves.definition;

import static org.awaitility.Awaitility.await;

import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.definition.eb.AddApiHandler;
import com.edgar.direwolves.definition.eb.ApiMatchHandler;
import com.edgar.direwolves.definition.eb.DeleteApiHandler;
import com.edgar.direwolves.definition.eb.GetApiHandler;
import com.edgar.direwolves.definition.eb.ListApiHandler;
import com.edgar.direwolves.definition.verticle.ApiDefinitionRegistry;
import com.edgar.direwolves.definition.verticle.ApiDefinitionVerticle;
import com.edgar.util.base.Randoms;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
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

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();
    eb = vertx.eventBus();
    vertx.deployVerticle(ApiDefinitionVerticle.class.getName(), context.asyncAssertSuccess());
  }

  @After
  public void clear(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
    ApiDefinitionRegistry.create().remove(null);
  }

  @Test
  public void testAddSuccess(TestContext context) {
    add(context);

    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
  }

  @Test
  public void testAddSuccess2(TestContext context) {
    JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add2.json");

//        Async async = context.async();
    eb.<JsonObject>send(AddApiHandler.ADDRESS, addDeviceJson, ar -> {
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
  }

  @Test
  public void testAddSameName(TestContext context) {
    JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");

    eb.<JsonObject>send(AddApiHandler.ADDRESS, addDeviceJson, ar -> {
      if (ar.succeeded()) {
        JsonObject jsonObject = ar.result().body();
        System.out.println(jsonObject);
        context.assertEquals("OK", jsonObject.getString("result"));
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });

    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
    await().until(() -> ApiDefinitionRegistry.create().filter("add_device").get(0)
            .method().equals(HttpMethod.POST));

    addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add2.json");

    eb.<JsonObject>send(AddApiHandler.ADDRESS, addDeviceJson, ar -> {
      if (ar.succeeded()) {
        JsonObject jsonObject = ar.result().body();
        System.out.println(jsonObject);
        context.assertEquals("OK", jsonObject.getString("result"));
      } else {
        System.out.println(ar.cause());
        context.fail();
      }
    });
    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
    await().until(() -> ApiDefinitionRegistry.create().filter("add_device").get(0)
            .method().equals(HttpMethod.PUT));
  }

  @Test
  public void testListAll(TestContext context) {
    add(context);
    add("src/test/resources/device_update.json", context);

    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);

    JsonObject queryJson = new JsonObject();
    eb.<List<ApiDefinition>>send(ListApiHandler.ADDRESS, queryJson, ar -> {
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
    eb.<List<ApiDefinition>>send(ListApiHandler.ADDRESS, queryJson, ar -> {
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
    eb.<List<ApiDefinition>>send(ListApiHandler.ADDRESS, queryJson, ar -> {
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
    eb.<List<ApiDefinition>>send(ListApiHandler.ADDRESS, queryJson, ar -> {
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
    eb.<List<ApiDefinition>>send(ListApiHandler.ADDRESS, queryJson, ar -> {
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

    eb.<ApiDefinition>send(GetApiHandler.ADDRESS, "add_device", ar -> {
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

    eb.<ApiDefinition>send(GetApiHandler.ADDRESS, "*device", ar -> {
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

    eb.<JsonObject>send(GetApiHandler.ADDRESS, Randoms.randomAlphabet(10), ar -> {
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
    eb.<JsonObject>send(DeleteApiHandler.ADDRESS, "*device", ar -> {
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
    eb.<JsonObject>send(DeleteApiHandler.ADDRESS, "*", ar -> {
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
    eb.<JsonObject>send(AddApiHandler.ADDRESS, addDeviceJson, ar -> {
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
