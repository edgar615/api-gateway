package com.edgar.direwolves.plugin;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import com.edgar.direwolves.plugin.ip.IpRestriction;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2017/7/6.
 *
 * @author Edgar  Date 2017/7/6
 */
public class FallbackPluginTest {

  @Test
  public void testDecode() {
    JsonObject jsonObject = new JsonObject()
            .put("request.fallback", new JsonObject().put("device.add", new JsonObject().put
                    ("foo", "bar"))
                    .put("device.query", new JsonArray()));
    ApiPluginFactory factory = new RequestFallbackPluginFactory();
    RequestFallbackPlugin plugin = (RequestFallbackPlugin) factory.decode(jsonObject);
    Assert.assertEquals(2, plugin.fallback().size());
  }

  @Test
  public void testEncode() {
    RequestFallbackPlugin plugin
            = (RequestFallbackPlugin)
            ApiPlugin.create(RequestFallbackPlugin.class.getSimpleName());
    plugin.setFallback( new JsonObject().put("device.add", new JsonObject().put
            ("foo", "bar"))
                                .put("device.query", new JsonArray()));

    JsonObject jsonObject = plugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("request.fallback"));
  }
}
