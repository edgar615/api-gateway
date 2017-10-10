//package com.github.edgar615.direwolves.plugin.ratelimit;
//
//import com.google.common.collect.Lists;
//
//import ApiDefinition;
//import HttpEndpoint;
//import com.github.edgar615.util.validation.ValidationException;
//import io.vertx.core.http.HttpMethod;
//import io.vertx.core.json.JsonObject;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * Created by Edgar on 2017/1/22.
// *
// * @author Edgar  Date 2017/1/22
// */
//public class AddRateLimiterCmdTest {
//
//  ApiDefinition definition;
//
//  @Before
//  public void setUp() {
//    HttpEndpoint httpEndpoint =
//            HttpEndpoint.http("get_device", HttpMethod.GET, "devices/", "device");
//
//    definition = ApiDefinition
//            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
//  }
//
//  @Test
//  public void testAddRateLimitToUndefinedRate() {
//    RateLimiterPlugin plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNull(plugin);
//
//    AddRateLimitCmd cmd = new AddRateLimitCmd();
//    JsonObject jsonObject = new JsonObject()
//            .put("key", "ip")
//            .put("type", "minute")
//            .put("limit", 1000);
//    cmd.handle(definition, jsonObject);
//
//    plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNotNull(plugin);
//    Assert.assertEquals(1, plugin.rateLimits().size());
//  }
//
//  @Test
//  public void testAddRateLimitToDefinedRate() {
//    RateLimiterPlugin plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNull(plugin);
//
//    AddRateLimitCmd cmd = new AddRateLimitCmd();
//    JsonObject jsonObject = new JsonObject()
//            .put("key", "ip")
//            .put("type", "minute")
//            .put("limit", 1000);
//    cmd.handle(definition, jsonObject);
//
//    plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNotNull(plugin);
//    Assert.assertEquals(1, plugin.rateLimits().size());
//
//    jsonObject = new JsonObject()
//            .put("key", "user")
//            .put("type", "minute")
//            .put("limit", 1000);
//    cmd.handle(definition, jsonObject);
//
//    plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNotNull(plugin);
//    Assert.assertEquals(2, plugin.rateLimits().size());
//  }
//
//  @Test
//  public void testReplaceRateLimitToDefinedRate() {
//    RateLimiterPlugin plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNull(plugin);
//
//    AddRateLimitCmd cmd = new AddRateLimitCmd();
//    JsonObject jsonObject = new JsonObject()
//            .put("key", "ip")
//            .put("type", "minute")
//            .put("limit", 1000);
//    cmd.handle(definition, jsonObject);
//
//    plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNotNull(plugin);
//    Assert.assertEquals(1, plugin.rateLimits().size());
//
//    jsonObject = new JsonObject()
//            .put("key", "ip")
//            .put("type", "minute")
//            .put("limit", 100);
//    cmd.handle(definition, jsonObject);
//
//    plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNotNull(plugin);
//    Assert.assertEquals(1, plugin.rateLimits().size());
//    Assert.assertEquals(100, plugin.rateLimits().get(0).limit());
//  }
//
//  @Test
//  public void missArgsShouldThrowValidationException() {
//    RateLimiterPlugin plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNull(plugin);
//
//    AddRateLimitCmd cmd = new AddRateLimitCmd();
//    JsonObject jsonObject = new JsonObject();
//    try {
//      cmd.handle(definition, jsonObject);
//      Assert.fail();
//    } catch (Exception e) {
//      e.printStackTrace();
//      Assert.assertTrue(e instanceof ValidationException);
//      ValidationException ex = (ValidationException) e;
//      Assert.assertTrue(ex.getErrorDetail().containsKey("key"));
//      Assert.assertTrue(ex.getErrorDetail().containsKey("type"));
//      Assert.assertTrue(ex.getErrorDetail().containsKey("limit"));
//    }
//  }
//
//  @Test
//  public void invalidArgsShouldThrowValidationException() {
//    RateLimiterPlugin plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNull(plugin);
//
//    AddRateLimitCmd cmd = new AddRateLimitCmd();
//    JsonObject jsonObject = new JsonObject()
//            .put("limit", 100)
//            .put("type", "foo")
//            .put("key", "unkown");
//    try {
//      cmd.handle(definition, jsonObject);
//      Assert.fail();
//    } catch (Exception e) {
//      e.printStackTrace();
//      Assert.assertTrue(e instanceof ValidationException);
//      ValidationException ex = (ValidationException) e;
//      Assert.assertTrue(ex.getErrorDetail().containsKey("key"));
//      Assert.assertTrue(ex.getErrorDetail().containsKey("type"));
//      Assert.assertFalse(ex.getErrorDetail().containsKey("limit"));
//    }
//  }
//
//}
