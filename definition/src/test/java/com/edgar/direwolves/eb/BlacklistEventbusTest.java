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
//import io.vertx.core.http.HttpMethod;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.unit.Async;
//import io.vertx.ext.unit.TestContext;
//import io.vertx.ext.unit.junit.VertxUnitRunner;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.List;
//
///**
// * Created by Edgar on 2016/9/13.
// *
// * @author Edgar  Date 2016/9/13
// */
//@RunWith(VertxUnitRunner.class)
//public class BlacklistEventbusTest {
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
//  public void testAddSuccess(TestContext context) {
//    add(context);
//
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
//  }
//
//  @Test
//  public void testAddSuccess2(TestContext context) {
//    JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add2.json");
//
////        Async async = context.async();
//    eb.<JsonObject>send(AddApiHandler.ADDRESS, addDeviceJson, ar -> {
//      if (ar.succeeded()) {
//        JsonObject jsonObject = ar.result().body();
//        System.out.println(jsonObject);
//        context.assertEquals("OK", jsonObject.getString("result"));
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
////            async.complete();
//    });
//
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
//  }
//
//  @Test
//  public void testAddSameName(TestContext context) {
//    JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");
//
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
//
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
//    await().until(() -> ApiDefinitionRegistry.create().filter("add_device").get(0)
//            .method().equals(HttpMethod.POST));
//
//    addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add2.json");
//
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
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
//    await().until(() -> ApiDefinitionRegistry.create().filter("add_device").get(0)
//            .method().equals(HttpMethod.PUT));
//  }
//
//  @Test
//  public void testListAll(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject queryJson = new JsonObject();
//    eb.<List<ApiDefinition>>send(ListApiHandler.ADDRESS, queryJson, ar -> {
//      if (ar.succeeded()) {
//        List<ApiDefinition> definitions = ar.result().body();
//        System.out.println(definitions);
//
//        context.assertEquals(2, definitions.size());
//        ApiDefinition apiDefinition = definitions.get(0);
//        context.assertEquals("add_device", apiDefinition.name());
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//  }
//
//  @Test
//  public void testListAll2(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject queryJson = new JsonObject().put("name", "*");
//    eb.<List<ApiDefinition>>send(ListApiHandler.ADDRESS, queryJson, ar -> {
//      if (ar.succeeded()) {
//        List<ApiDefinition> definitions = ar.result().body();
//        System.out.println(definitions);
//
//        context.assertEquals(2, definitions.size());
//        ApiDefinition apiDefinition = definitions.get(0);
//        context.assertEquals("add_device", apiDefinition.name());
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//
//  }
//
//  @Test
//  public void testListEmpty(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject queryJson = new JsonObject().put("name", Randoms.randomAlphabet(10));
//    eb.<List<ApiDefinition>>send(ListApiHandler.ADDRESS, queryJson, ar -> {
//      if (ar.succeeded()) {
//        List<ApiDefinition> definitions = ar.result().body();
//        System.out.println(definitions);
//
//        context.assertEquals(0, definitions.size());
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//
//  }
//
//  @Test
//  public void testListStart(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject queryJson = new JsonObject().put("name", "*")
//            .put("start", 1);
//    eb.<List<ApiDefinition>>send(ListApiHandler.ADDRESS, queryJson, ar -> {
//      if (ar.succeeded()) {
//        List<ApiDefinition> definitions = ar.result().body();
//        System.out.println(definitions);
//
//        context.assertEquals(1, definitions.size());
//        ApiDefinition apiDefinition = definitions.get(0);
//        context.assertEquals("update_device", apiDefinition.name());
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//
//  }
//
//  @Test
//  public void testListLimit(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject queryJson = new JsonObject().put("name", "*")
//            .put("limit", 1);
//    eb.<List<ApiDefinition>>send(ListApiHandler.ADDRESS, queryJson, ar -> {
//      if (ar.succeeded()) {
//        List<ApiDefinition> definitions = ar.result().body();
//        System.out.println(definitions);
//
//        context.assertEquals(1, definitions.size());
//        ApiDefinition apiDefinition = definitions.get(0);
//        context.assertEquals("add_device", apiDefinition.name());
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//
//  }
//
//  @Test
//  public void testGet(TestContext context) {
//    add(context);
//
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
//
//    Async async = context.async();
//
//    eb.<ApiDefinition>send(GetApiHandler.ADDRESS, "add_device", ar -> {
//      if (ar.succeeded()) {
//        ApiDefinition apiDefinition = ar.result().body();
//        context.assertEquals("add_device", apiDefinition.name());
//      } else {
//        ar.cause().printStackTrace();
//        System.out.println(ar.cause());
//        context.fail();
//      }
//      async.complete();
//    });
//
//    Async async2 = context.async();
//
//    eb.<ApiDefinition>send(GetApiHandler.ADDRESS, "*device", ar -> {
//      if (ar.succeeded()) {
//        ApiDefinition apiDefinition = ar.result().body();
//        context.assertEquals("add_device", apiDefinition.name());
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//      async2.complete();
//    });
//  }
//
//  @Test
//  public void testGet404(TestContext context) {
//    add(context);
//
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() > 0);
//
//    Async async = context.async();
//
//    eb.<JsonObject>send(GetApiHandler.ADDRESS, Randoms.randomAlphabet(10), ar -> {
//      if (ar.succeeded()) {
//        context.fail();
//      } else {
//        System.out.println(ar.cause());
//      }
//      async.complete();
//    });
//  }
//
//  @Test
//  public void testDelete(TestContext context) {
//    add(context);
//    eb.<JsonObject>send(DeleteApiHandler.ADDRESS, "*device", ar -> {
//      if (ar.succeeded()) {
//        JsonObject jsonObject = ar.result().body();
//        context.assertEquals("OK", jsonObject.getString("result"));
//        context.assertEquals(0, ApiDefinitionRegistry.create().getDefinitions()
//                .size());
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 0);
//  }
//
//  @Test
//  public void testDeleteAll(TestContext context) {
//    add(context);
//    eb.<JsonObject>send(DeleteApiHandler.ADDRESS, "*", ar -> {
//      if (ar.succeeded()) {
//        JsonObject jsonObject = ar.result().body();
//        context.assertEquals("OK", jsonObject.getString("result"));
//        context.assertEquals(0, ApiDefinitionRegistry.create().getDefinitions()
//                .size());
//      } else {
//        System.out.println(ar.cause());
//        context.fail();
//      }
//    });
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 0);
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
//    await().until(() -> apiDefinition.filters().contains(filter));
//
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> !apiDefinition2.filters().contains(filter));
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
//    await().until(() -> apiDefinition.filters().contains(filter));
//
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.filters().contains(filter));
//  }
//
//  @Test
//  public void testDeleteFilter(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
//    await().until(() -> apiDefinition.filters().contains("jwt"));
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
//    await().until(() -> !apiDefinition2.filters().contains("jwt"));
//  }
//
//  @Test
//  public void testDeleteFilterOfAllApi(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//    ApiDefinition apiDefinition = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition);
//    await().until(() -> apiDefinition.filters().contains("jwt"));
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
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("add_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> !apiDefinition2.filters().contains("jwt"));
//
//    ApiDefinition apiDefinition3 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition3);
//    await().until(() -> !apiDefinition3.filters().contains("jwt"));
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
//  @Test
//  public void testAddBlackIp(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "add_device")
//            .put("ip", "192.168.*.*");
//
//    eb.<JsonObject>send(AddBlacklistHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> apiDefinition.blacklist().contains("192.168.*.*"));
//  }
//
//  @Test
//  public void testAddBlackIpToAll(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject addDeviceJson = new JsonObject()
//            .put("ip", "192.168.*.*");
//
//    eb.<JsonObject>send(AddBlacklistHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> apiDefinition.blacklist().contains("192.168.*.*"));
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.blacklist().contains("192.168.*.*"));
//  }
//
//  @Test
//  public void testDeleteBlackIp(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "add_device")
//            .put("ip", "192.168.0.100");
//
//    eb.<JsonObject>send(DeleteBlacklistHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> !apiDefinition.blacklist().contains("192.168.0.100"));
//  }
//
//  @Test
//  public void testDeleteBlackIpToAll(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject addDeviceJson = new JsonObject()
//            .put("ip", "192.168.0.100");
//
//    eb.<JsonObject>send(DeleteBlacklistHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> !apiDefinition.blacklist().contains("192.168.0.100"));
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> !apiDefinition2.blacklist().contains("192.168.0.100"));
//  }
//
//  @Test
//  public void testDeleteAllBlackIp(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "add*");
//
//    eb.<JsonObject>send(DeleteBlacklistHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> apiDefinition.blacklist().size() == 0);
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.blacklist().contains("192.168.0.100"));
//  }
//
//  @Test
//  public void testAddWhiteIp(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "add_device")
//            .put("ip", "192.168.*.*");
//
//    eb.<JsonObject>send(AddWhitelistHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> apiDefinition.whitelist().contains("192.168.*.*"));
//  }
//
//  @Test
//  public void testAddWhiteIpToAll(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject addDeviceJson = new JsonObject()
//            .put("ip", "192.168.*.*");
//
//    eb.<JsonObject>send(AddWhitelistHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> apiDefinition.whitelist().contains("192.168.*.*"));
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.whitelist().contains("192.168.*.*"));
//  }
//
//  @Test
//  public void testDeleteWhiteIp(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "add_device")
//            .put("ip", "192.168.0.1");
//
//    eb.<JsonObject>send(DeleteWhitelistHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> !apiDefinition.whitelist().contains("192.168.0.1"));
//  }
//
//  @Test
//  public void testDeleteWhiteIpToAll(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject addDeviceJson = new JsonObject()
//            .put("ip", "192.168.0.1");
//
//    eb.<JsonObject>send(DeleteWhitelistHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> !apiDefinition.whitelist().contains("192.168.0.1"));
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> !apiDefinition2.whitelist().contains("192.168.0.1"));
//  }
//
//  @Test
//  public void testDeleteAllWhiteIp(TestContext context) {
//    add(context);
//    add("src/test/resources/device_update.json", context);
//    await().until(() -> ApiDefinitionRegistry.create().filter(null).size() == 2);
//
//    JsonObject addDeviceJson = new JsonObject()
//            .put("name", "*device");
//
//    eb.<JsonObject>send(DeleteWhitelistHandler.ADDRESS, addDeviceJson, ar -> {
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
//    await().until(() -> apiDefinition.whitelist().size() == 0);
//    ApiDefinition apiDefinition2 = ApiDefinitionRegistry.create().filter("update_device").get(0);
//    System.out.println(apiDefinition2);
//    await().until(() -> apiDefinition2.whitelist().size() == 0);
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
