package com.github.edgar615.gateway.plugin.fallback;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import com.github.edgar615.gateway.core.rpc.RpcResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by Edgar on 2017/7/6.
 *
 * @author Edgar  Date 2017/7/6
 */
public class FallbackPluginTest {

  @Test
  public void testDecode() {
    JsonObject jsonObject = new JsonObject()
            .put("request.fallback", new JsonObject()
                    .put("device.add",
                         new JsonObject().put("result", new JsonObject().put("foo", "bar")))
                    .put("device.query",
                         new JsonObject().put("statusCode", 400).put("result", new JsonArray())));
    ApiPluginFactory factory = new FallbackPluginFactory();
    FallbackPlugin plugin = (FallbackPlugin) factory.decode(jsonObject);
    Assert.assertEquals(2, plugin.fallback().values().size());
    Assert.assertEquals(200, plugin.fallback().get("device.add").statusCode());
    Assert.assertFalse(plugin.fallback().get("device.add").isArray());
    Assert.assertEquals(400, plugin.fallback().get("device.query").statusCode());
    Assert.assertTrue(plugin.fallback().get("device.query").isArray());
  }

  @Test
  public void testEncode() {
    FallbackPlugin plugin
            = (FallbackPlugin)
            ApiPlugin.create(FallbackPlugin.class.getSimpleName());
    plugin.addFallBack("device.add", RpcResponse.create(UUID.randomUUID().toString(),
                                                        200,
                                                        new JsonObject().put("foo", "bar").encode(),
                                                        0));
    plugin.addFallBack("device.query", RpcResponse.create(UUID.randomUUID().toString(),
                                                        400,
                                                        new JsonArray().encode(),
                                                        0));

    JsonObject jsonObject = plugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("request.fallback"));
    Assert.assertEquals(2, jsonObject.getJsonObject("request.fallback").fieldNames().size());
  }
}
