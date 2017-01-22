package com.edgar.direwolves.plugin.ratelimit;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Edgar on 2017/1/22.
 *
 * @author Edgar  Date 2017/1/22
 */
public class DeleteRateLimitCmdTest {

  ApiDefinition definition;

  @Before
  public void setUp() {
    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");

    definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));

    RateLimit rateLimit = RateLimit.create("ip", "minute", 1000);
    RateLimitPlugin plugin = RateLimitPlugin.create();
    plugin.addRateLimit(rateLimit);

    definition.addPlugin(plugin);
  }

  @Test
  public void testDeleteRateLimitToUndefinedRate() {
    RateLimitPlugin plugin =
            (RateLimitPlugin) definition.plugin(RateLimitPlugin.class.getSimpleName());
    Assert.assertEquals(1, plugin.rateLimits().size());

    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
    JsonObject jsonObject = new JsonObject()
            .put("key", "ip")
            .put("type", "second");
    cmd.handle(definition, jsonObject);

    plugin =
            (RateLimitPlugin) definition.plugin(RateLimitPlugin.class.getSimpleName());
    Assert.assertNotNull(plugin);
    Assert.assertEquals(1, plugin.rateLimits().size());
  }

  @Test
  public void testDeleteRateLimitToDefinedRate() {
    RateLimitPlugin plugin =
            (RateLimitPlugin) definition.plugin(RateLimitPlugin.class.getSimpleName());
    Assert.assertEquals(1, plugin.rateLimits().size());

    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
    JsonObject jsonObject = new JsonObject()
            .put("key", "ip")
            .put("type", "minute");
    cmd.handle(definition, jsonObject);

    plugin =
            (RateLimitPlugin) definition.plugin(RateLimitPlugin.class.getSimpleName());
    Assert.assertNotNull(plugin);
    Assert.assertEquals(0, plugin.rateLimits().size());
  }

  @Test
  public void testDeleteByKeyRateLimitToDefinedRate() {
    RateLimitPlugin plugin =
            (RateLimitPlugin) definition.plugin(RateLimitPlugin.class.getSimpleName());
    Assert.assertEquals(1, plugin.rateLimits().size());

    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
    JsonObject jsonObject = new JsonObject()
            .put("key", "ip");
    cmd.handle(definition, jsonObject);

    plugin =
            (RateLimitPlugin) definition.plugin(RateLimitPlugin.class.getSimpleName());
    Assert.assertNotNull(plugin);
    Assert.assertEquals(0, plugin.rateLimits().size());
  }

  @Test
  public void testDeleteByTypeRateLimitToDefinedRate() {
    RateLimitPlugin plugin =
            (RateLimitPlugin) definition.plugin(RateLimitPlugin.class.getSimpleName());
    Assert.assertEquals(1, plugin.rateLimits().size());

    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
    JsonObject jsonObject = new JsonObject()
            .put("type", "minute");
    cmd.handle(definition, jsonObject);

    plugin =
            (RateLimitPlugin) definition.plugin(RateLimitPlugin.class.getSimpleName());
    Assert.assertNotNull(plugin);
    Assert.assertEquals(0, plugin.rateLimits().size());
  }

  @Test
  public void testDeleteAllByTypeRateLimitToDefinedRate() {
    RateLimitPlugin plugin =
            (RateLimitPlugin) definition.plugin(RateLimitPlugin.class.getSimpleName());
    Assert.assertEquals(1, plugin.rateLimits().size());

    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
    JsonObject jsonObject = new JsonObject();
    cmd.handle(definition, jsonObject);

    plugin =
            (RateLimitPlugin) definition.plugin(RateLimitPlugin.class.getSimpleName());
    Assert.assertNotNull(plugin);
    Assert.assertEquals(0, plugin.rateLimits().size());
  }

  @Test
  public void invalidArgsShouldThrowValidationException() {
    DeleteRateLimitCmd cmd = new DeleteRateLimitCmd();
    JsonObject jsonObject = new JsonObject()
            .put("type", "foo")
            .put("key", "unkown");
    try {
      cmd.handle(definition, jsonObject);
      Assert.fail();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.assertTrue(e instanceof ValidationException);
      ValidationException ex = (ValidationException) e;
      Assert.assertTrue(ex.getErrorDetail().containsKey("key"));
      Assert.assertTrue(ex.getErrorDetail().containsKey("type"));
      Assert.assertFalse(ex.getErrorDetail().containsKey("limit"));
    }
  }

}
