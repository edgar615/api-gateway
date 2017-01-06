//package com.edgar.direwolves.definition;
//
//import static org.awaitility.Awaitility.await;
//
//import com.edgar.direwolves.core.utils.JsonUtils;
//import com.edgar.direwolves.definition.eb.*;
//import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
//import com.edgar.direwolves.verticle.ApiDefinitionVerticle;
//import com.edgar.util.base.Randoms;
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
//public class FilterEventbusTest {
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
//  public void testAddFilter(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    String filter = Randoms.randomAlphabet(5);
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "add_device")
//            .put("filter", filter);
//
//    eb.<JsonObject>send(AddFilterHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
////    await().until(() -> apiDefinition.filters().contains(filter));
////
////    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get
// (0);
////    System.out.println(apiDefinition2);
////    await().until(() -> !apiDefinition2.filters().contains(filter));
//  }
//
//  @Test
//  public void testAddFilterToAll(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    String filter = Randoms.randomAlphabet(5);
//    JsonObject addDeviceJson = new JsonObject()
//            .put("filter", filter);
//
//    eb.<JsonObject>send(AddFilterHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
////    await().until(() -> apiDefinition.filters().contains(filter));
////
////    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get
// (0);
////    System.out.println(apiDefinition2);
////    await().until(() -> apiDefinition2.filters().contains(filter));
//  }
//
//  @Test
//  public void testDeleteFilter(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
////    await().until(() -> apiDefinition.filters().contains("jwt"));
//
//    eb.<JsonObject>send(DeleteFilterHandler.ADDRESS,
//                        new JsonObject().put("name", "add_device").put("filter", "jwt"), ar -> {
//              if (ar.succeeded()) {
//                JsonObject jsonObject = ar.result().body();
//                System.out.println(jsonObject);
//                context.assertEquals("OK", jsonObject.getString("result"));
//              } else {
//                System.out.println(ar.cause());
//                context.fail();
//              }
//            });
//
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition2);
////    await().until(() -> !apiDefinition2.filters().contains("jwt"));
//  }
//
//  @Test
//  public void testDeleteFilterOfAllApi(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
////    await().until(() -> apiDefinition.filters().contains("jwt"));
//
//    eb.<JsonObject>send(DeleteFilterHandler.ADDRESS,
//                        new JsonObject().put("filter", "jwt"), ar -> {
//              if (ar.succeeded()) {
//                JsonObject jsonObject = ar.result().body();
//                System.out.println(jsonObject);
//                context.assertEquals("OK", jsonObject.getString("result"));
//              } else {
//                System.out.println(ar.cause());
//                context.fail();
//              }
//            });
//
////    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("add_device").get(0);
////    System.out.println(apiDefinition2);
////    await().until(() -> !apiDefinition2.filters().contains("jwt"));
////
////    ApiDefinition apiDefinition3 = ApiDefinitionRegistry.create().filter("update_device").get
// (0);
////    System.out.println(apiDefinition3);
////    await().until(() -> !apiDefinition3.filters().contains("jwt"));
//  }
//
//  @Test
//  public void testDeleteAllFilter(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
//    await().until(() -> apiDefinition.filters().contains("jwt"));
//
//    eb.<JsonObject>send(DeleteFilterHandler.ADDRESS,
//                        new JsonObject().put("name", "add_device"), ar -> {
//              if (ar.succeeded()) {
//                JsonObject jsonObject = ar.result().body();
//                System.out.println(jsonObject);
//                context.assertEquals("OK", jsonObject.getString("result"));
//              } else {
//                System.out.println(ar.cause());
//                context.fail();
//              }
//            });
//
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.filters().size() == 0);
//
//    ApiDefinition apiDefinition3 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition3);
//    await().until(() -> apiDefinition3.filters().contains("jwt"));
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
