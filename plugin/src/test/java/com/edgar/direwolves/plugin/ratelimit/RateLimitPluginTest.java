package com.edgar.direwolves.plugin.ratelimit;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import com.edgar.direwolves.plugin.ratelimit.RateLimit;
import com.edgar.direwolves.plugin.ratelimit.RateLimitPlugin;
import com.edgar.direwolves.plugin.ratelimit.RateLimitPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public class RateLimitPluginTest {

  @Test
  public void testDecode() {
    JsonArray jsonArray = new JsonArray();
    JsonObject jsonObject = new JsonObject()
        .put("rate_limit", jsonArray);
    jsonArray.add(new JsonObject().put("type", "second")
        .put("limit", 100)
        .put("limit_by", "token"));
    ApiPluginFactory<RateLimitPlugin> factory = new RateLimitPluginFactory();
    RateLimitPlugin plugin = factory.decode(jsonObject);
    Assert.assertEquals(1, plugin.rateLimits().size());
  }

  @Test
  public void testEncode() {
    ApiPlugin plugin = ApiPlugin.create(RateLimitPlugin.class.getSimpleName());
    RateLimitPlugin rateLimitPlugin = (RateLimitPlugin) plugin;
    rateLimitPlugin.addRateLimit(RateLimit.create("token", "second", 100));
    rateLimitPlugin.addRateLimit(RateLimit.create("user", "day", 100));
    Assert.assertEquals(2, rateLimitPlugin.rateLimits().size());
    JsonObject jsonObject = rateLimitPlugin.encode();
    Assert.assertTrue(jsonObject.containsKey("rate_limit"));
    JsonArray jsonArray = jsonObject.getJsonArray("rate_limit");
    Assert.assertEquals(2, jsonArray.size());
    System.out.println(jsonObject);
  }

  @Test
  public void testRemove() {
    ApiPlugin plugin = ApiPlugin.create(RateLimitPlugin.class.getSimpleName());
    RateLimitPlugin rateLimitPlugin = (RateLimitPlugin) plugin;
    rateLimitPlugin.addRateLimit(RateLimit.create("token", "second", 100));
    rateLimitPlugin.addRateLimit(RateLimit.create("user", "day", 100));
    Assert.assertEquals(2, rateLimitPlugin.rateLimits().size());

    rateLimitPlugin.removeRateLimit("user", "second");
    rateLimitPlugin.addRateLimit(RateLimit.create("token", "second", 1000));
    Assert.assertEquals(2, rateLimitPlugin.rateLimits().size());

    RateLimit rateLimit = rateLimitPlugin.rateLimits().stream()
        .filter(r -> "token".equalsIgnoreCase(r.limitBy()))
        .findAny().get();
    Assert.assertEquals(1000, rateLimit.limit());

    rateLimitPlugin.removeRateLimit("user", null);
    Assert.assertEquals(1, rateLimitPlugin.rateLimits().size());
  }

  @Test
  public void testRemoveAll() {
    ApiPlugin plugin = ApiPlugin.create(RateLimitPlugin.class.getSimpleName());
    RateLimitPlugin rateLimitPlugin = (RateLimitPlugin) plugin;
    rateLimitPlugin.addRateLimit(RateLimit.create("token", "second", 100));
    rateLimitPlugin.addRateLimit(RateLimit.create("user", "day", 100));
    Assert.assertEquals(2, rateLimitPlugin.rateLimits().size());

    rateLimitPlugin.removeRateLimit(null, null);
    Assert.assertEquals(0, rateLimitPlugin.rateLimits().size());
  }


  @Test
  public void testUniqueRateLimit() {
    ApiPlugin plugin = ApiPlugin.create(RateLimitPlugin.class.getSimpleName());
    RateLimitPlugin rateLimitPlugin = (RateLimitPlugin) plugin;

    rateLimitPlugin.addRateLimit(RateLimit.create("token", "second", 100));
    rateLimitPlugin.addRateLimit(RateLimit.create("token", "day", 100));
    rateLimitPlugin.addRateLimit(RateLimit.create("user", "second", 100));
    Assert.assertEquals(3, rateLimitPlugin.rateLimits().size());

    rateLimitPlugin.addRateLimit(RateLimit.create("token", "second", 1000));
    rateLimitPlugin.addRateLimit(RateLimit.create("token", "day", 1000));
    rateLimitPlugin.addRateLimit(RateLimit.create("user", "second", 1000));
    Assert.assertEquals(3, rateLimitPlugin.rateLimits().size());

    List<RateLimit> filterDefintions = rateLimitPlugin.rateLimits().stream()
        .filter(d -> "token".equalsIgnoreCase(d.limitBy())
            && "day".equalsIgnoreCase(d.type()))
        .collect(Collectors.toList());
    RateLimit rateLimit = filterDefintions.get(0);
    Assert.assertEquals(1000, rateLimit.limit());
  }

}
