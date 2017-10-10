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
//public class DeleteRateLimiterCmdTest {
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
//
//    RateLimiterPolicy rateLimit = RateLimiterPolicy.create("ip", "minute", 1000);
//    RateLimiterPlugin plugin = RateLimiterPlugin.create();
//    plugin.addRateLimiter(rateLimit);
//
//    definition.addPlugin(plugin);
//  }
//
//  @Test
//  public void testDeleteRateLimitToUndefinedRate() {
//    RateLimiterPlugin plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertEquals(1, plugin.rateLimits().size());
//
//    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
//    JsonObject jsonObject = new JsonObject()
//            .put("key", "ip")
//            .put("type", "second");
//    cmd.handle(definition, jsonObject);
//
//    plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNotNull(plugin);
//    Assert.assertEquals(1, plugin.rateLimits().size());
//  }
//
//  @Test
//  public void testDeleteRateLimitToDefinedRate() {
//    RateLimiterPlugin plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertEquals(1, plugin.rateLimits().size());
//
//    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
//    JsonObject jsonObject = new JsonObject()
//            .put("key", "ip")
//            .put("type", "minute");
//    cmd.handle(definition, jsonObject);
//
//    plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNotNull(plugin);
//    Assert.assertEquals(0, plugin.rateLimits().size());
//  }
//
//  @Test
//  public void testDeleteByKeyRateLimitToDefinedRate() {
//    RateLimiterPlugin plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertEquals(1, plugin.rateLimits().size());
//
//    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
//    JsonObject jsonObject = new JsonObject()
//            .put("key", "ip");
//    cmd.handle(definition, jsonObject);
//
//    plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNotNull(plugin);
//    Assert.assertEquals(0, plugin.rateLimits().size());
//  }
//
//  @Test
//  public void testDeleteByTypeRateLimitToDefinedRate() {
//    RateLimiterPlugin plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertEquals(1, plugin.rateLimits().size());
//
//    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
//    JsonObject jsonObject = new JsonObject()
//            .put("type", "minute");
//    cmd.handle(definition, jsonObject);
//
//    plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNotNull(plugin);
//    Assert.assertEquals(0, plugin.rateLimits().size());
//  }
//
//  @Test
//  public void testDeleteAllByTypeRateLimitToDefinedRate() {
//    RateLimiterPlugin plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertEquals(1, plugin.rateLimits().size());
//
//    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
//    JsonObject jsonObject = new JsonObject();
//    cmd.handle(definition, jsonObject);
//
//    plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    Assert.assertNotNull(plugin);
//    Assert.assertEquals(0, plugin.rateLimits().size());
//  }
//
//  @Test
//  public void invalidArgsShouldThrowValidationException() {
//    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
//    JsonObject jsonObject = new JsonObject()
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
