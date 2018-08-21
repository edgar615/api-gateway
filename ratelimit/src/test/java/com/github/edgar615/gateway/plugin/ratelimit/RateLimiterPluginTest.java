package com.github.edgar615.gateway.plugin.ratelimit;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public class RateLimiterPluginTest {

    @Test
    public void testDecode() {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject()
                .put("rate.limiter", jsonArray);
        jsonArray.add(new JsonObject().put("name", "second")
                              .put("burst", 100));
        ApiPluginFactory factory = new RateLimiterPluginFactory();
        RateLimiterPlugin plugin = (RateLimiterPlugin) factory.decode(jsonObject);
        Assert.assertEquals(1, plugin.rateLimiters().size());
        Assert.assertEquals(100, plugin.rateLimiters().get(0).burst());
        Assert.assertEquals("second", plugin.rateLimiters().get(0).name());
    }

    @Test
    public void testEncode() {
        ApiPlugin plugin = ApiPlugin.create(RateLimiterPlugin.class.getSimpleName());
        RateLimiterPlugin rateLimiterPlugin = (RateLimiterPlugin) plugin;
        rateLimiterPlugin.addRateLimiter(new RateLimiter("second", 100));
        rateLimiterPlugin.addRateLimiter(new RateLimiter("user", 10000));
        Assert.assertEquals(2, rateLimiterPlugin.rateLimiters().size());
        JsonObject jsonObject = rateLimiterPlugin.encode();
        Assert.assertTrue(jsonObject.containsKey("rate.limiter"));
        JsonArray jsonArray = jsonObject.getJsonArray("rate.limiter");
        Assert.assertEquals(2, jsonArray.size());
        System.out.println(jsonObject);
    }

    @Test
    public void testRemove() {
        ApiPlugin plugin = ApiPlugin.create(RateLimiterPlugin.class.getSimpleName());
        RateLimiterPlugin rateLimiterPlugin = (RateLimiterPlugin) plugin;
        rateLimiterPlugin.addRateLimiter(new RateLimiter("second", 100));
        rateLimiterPlugin.addRateLimiter(new RateLimiter("user", 10000));
        Assert.assertEquals(2, rateLimiterPlugin.rateLimiters().size());

        rateLimiterPlugin.removeRateLimiter("user");
        rateLimiterPlugin.addRateLimiter(new RateLimiter("token", 200));
        Assert.assertEquals(2, rateLimiterPlugin.rateLimiters().size());

        RateLimiter rateLimiter = rateLimiterPlugin.rateLimiters().stream()
                .filter(r -> "token".equalsIgnoreCase(r.name()))
                .findAny().get();
        Assert.assertEquals(200, rateLimiter.burst());

        rateLimiterPlugin.removeRateLimiter("token");
        Assert.assertEquals(1, rateLimiterPlugin.rateLimiters().size());
    }

    @Test
    public void testUniqueRateLimit() {
        ApiPlugin plugin = ApiPlugin.create(RateLimiterPlugin.class.getSimpleName());
        RateLimiterPlugin rateLimiterPlugin = (RateLimiterPlugin) plugin;
        rateLimiterPlugin.addRateLimiter(new RateLimiter("second", 100));
        rateLimiterPlugin.addRateLimiter(new RateLimiter("user", 10000));
        Assert.assertEquals(2, rateLimiterPlugin.rateLimiters().size());

        RateLimiter rateLimiter = rateLimiterPlugin.rateLimiters().stream()
                .filter(r -> "second".equalsIgnoreCase(r.name()))
                .findAny().get();
        Assert.assertEquals(100, rateLimiter.burst());

        rateLimiterPlugin.addRateLimiter(new RateLimiter("second", 200));
        Assert.assertEquals(2, rateLimiterPlugin.rateLimiters().size());

        rateLimiter = rateLimiterPlugin.rateLimiters().stream()
                .filter(r -> "second".equalsIgnoreCase(r.name()))
                .findAny().get();
        Assert.assertEquals(200, rateLimiter.burst());
    }

}
