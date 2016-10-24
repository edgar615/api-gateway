//package com.edgar.direwolves.definition;
//
//import static org.awaitility.Awaitility.await;
//
//import com.edgar.direwolves.core.utils.JsonUtils;
//import com.edgar.direwolves.definition.eb.*;
//import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
//import com.edgar.direwolves.verticle.ApiDefinitionVerticle;
//import io.vertx.core.Vertx;
//import io.vertx.core.eventbus.EventBus;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.unit.TestContext;
//import io.vertx.ext.unit.junit.VertxUnitRunner;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
///**
// * Created by Edgar on 2016/9/13.
// *
// * @author Edgar  Date 2016/9/13
// */
//@RunWith(VertxUnitRunner.class)
//public class RateLimitEbTest {
//
//  Vertx vertx;
//
//  EventBus eb;
//
//  @Before
//  public void setUp(TestContext context) {
//    vertx = Vertx.vertx();
//    eb = vertx.eventBus();
//    vertx.deployVerticle(ApiDefinitionVerticle.class.getName(), context.asyncAssertSuccess());
//  }
//
//  @After
//  public void clear(TestContext context) {
//    vertx.close(context.asyncAssertSuccess());
//    ApiDefinitionRegistry.create().remove(null);
//  }
//
//  @Test
//  public void testAddRateLimitSuccess(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "add_device")
//            .put("type", "day")
//            .put("limit_by", "user")
//            .put("limit", 1000);
//
//    eb.<JsonObject>send(AddRateLimitHandler.ADDRESS, addDeviceJson, ar -> {
//      if (ar.succeeded()) {
//        JsonObject jsonObject = ar.result().body();
//        System.out.println(jsonObject);
//        context.assertEquals("OK", jsonObject.getString("result"));
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
//    await().until(() -> apiDefinition.rateLimits().size() == 3);
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.rateLimits().size() == 2);
//  }
//
//  @Test
//  public void testAddRateLimitToAll(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    JsonObject addDeviceJson = new JsonObject()
//            .put("type", "day")
//            .put("limit_by", "user")
//            .put("limit", 1000);
//
//    eb.<JsonObject>send(AddRateLimitHandler.ADDRESS, addDeviceJson, ar -> {
//      if (ar.succeeded()) {
//        JsonObject jsonObject = ar.result().body();
//        System.out.println(jsonObject);
//        context.assertEquals("OK", jsonObject.getString("result"));
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
//    await().until(() -> apiDefinition.rateLimits().size() == 3);
//
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.rateLimits().size() == 3);
//  }
//
//  @Test
//  public void testDeleteRateLimitSuccess(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "add_device")
//            .put("type", "hour")
//            .put("limit_by", "token")
//            .put("limit", 1000);
//
//    eb.<JsonObject>send(DeleteRateLimitHandler.ADDRESS, addDeviceJson, ar -> {
//      if (ar.succeeded()) {
//        JsonObject jsonObject = ar.result().body();
//        System.out.println(jsonObject);
//        context.assertEquals("OK", jsonObject.getString("result"));
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
//    await().until(() -> apiDefinition.rateLimits().size() == 1);
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.rateLimits().size() == 2);
//  }
//
//  @Test
//  public void testDeleteRateLimitByType(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "add_device")
//            .put("type", "hour")
//            .put("limit", 1000);
//
//    eb.<JsonObject>send(DeleteRateLimitHandler.ADDRESS, addDeviceJson, ar -> {
//      if (ar.succeeded()) {
//        JsonObject jsonObject = ar.result().body();
//        System.out.println(jsonObject);
//        context.assertEquals("OK", jsonObject.getString("result"));
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
//    await().until(() -> apiDefinition.rateLimits().size() == 1);
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.rateLimits().size() == 2);
//  }
//
//  @Test
//  public void testDeleteRateLimitByLimitBy(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "add_device")
//            .put("limit_by", "token")
//            .put("limit", 1000);
//
//    eb.<JsonObject>send(DeleteRateLimitHandler.ADDRESS, addDeviceJson, ar -> {
//      if (ar.succeeded()) {
//        JsonObject jsonObject = ar.result().body();
//        System.out.println(jsonObject);
//        context.assertEquals("OK", jsonObject.getString("result"));
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
//    await().until(() -> apiDefinition.rateLimits().size() == 1);
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.rateLimits().size() == 2);
//  }
//
//  @Test
//  public void testDeleteRateLimitNotExists(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "add_device")
//            .put("limit_by", "tokens")
//            .put("type", "hour")
//            .put("limit", 1000);
//
//    eb.<JsonObject>send(DeleteRateLimitHandler.ADDRESS, addDeviceJson, ar -> {
//      if (ar.succeeded()) {
//        JsonObject jsonObject = ar.result().body();
//        System.out.println(jsonObject);
//        context.assertEquals("OK", jsonObject.getString("result"));
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
//    await().until(() -> apiDefinition.rateLimits().size() == 2);
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.rateLimits().size() == 2);
//  }
//
//  @Test
//  public void testDeleteAllRateLimit(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    JsonObject addDeviceJson = new JsonObject();
//
//    eb.<JsonObject>send(DeleteRateLimitHandler.ADDRESS, addDeviceJson, ar -> {
//      if (ar.succeeded()) {
//        JsonObject jsonObject = ar.result().body();
//        System.out.println(jsonObject);
//        context.assertEquals("OK", jsonObject.getString("result"));
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
//    await().until(() -> apiDefinition.rateLimits().size() == 0);
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.rateLimits().size() == 0);
//  }
//
//  private void add(String file, TestContext context) {
//    JsonObject
//            addDeviceJson = JsonUtils.getJsonFromFile(file);
//    eb.<JsonObject>send(AddApiHandler.ADDRESS, addDeviceJson, ar -> {
//      if (ar.succeeded()) {
//        JsonObject jsonObject = ar.result().body();
//        System.out.println(jsonObject);
//        context.assertEquals("OK", jsonObject.getString("result"));
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//  }
//
//  private void add(TestContext context) {
//    add("src/test/resources/device_add.json", context);
//  }
//}
