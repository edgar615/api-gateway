package com.edgar.direwolves.plugin;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.plugin.transformer.RequestTransformerPlugin;
import com.edgar.direwolves.plugin.transformer.ResponseTransformerPlugin;
import com.edgar.direwolves.plugin.transformer.ResponseTransformerPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by edgar on 16-10-23.
 */
public class ResponseTransformerPluginTest {
  @Test
  public void testDecode() {
    JsonObject request = new JsonObject()
            .put("header.remove", new JsonArray().add("h3").add("h4"))
            .put("body.remove", new JsonArray().add("p3").add("p4"))
            .put("header.replace", new JsonArray().add("h5:v2").add("h6:v1"))
            .put("body.replace", new JsonArray().add("p5:v2").add("p6:v1"))
            .put("header.add", new JsonArray().add("h1:v2").add("h2:v1"))
            .put("body.add", new JsonArray().add("p1:v2").add("p2:v1"));
    ResponseTransformerPlugin plugin = new ResponseTransformerPluginFactory()
            .decode(new JsonObject().put("response_transformer", request));
    Assert.assertEquals(2, plugin.headerRemoved().size());
    Assert.assertEquals(2, plugin.bodyRemoved().size());
    Assert.assertEquals(2, plugin.headerReplaced().size());
    Assert.assertEquals(2, plugin.bodyReplaced().size());
    Assert.assertEquals(2, plugin.headerAdded().size());
    Assert.assertEquals(2, plugin.bodyAdded().size());
  }

  @Test
  public void testEncode() {
    ResponseTransformerPlugin plugin = (ResponseTransformerPlugin) ApiPlugin
            .create(ResponseTransformerPlugin.class.getSimpleName());

    ResponseTransformerPlugin transformer =
            (ResponseTransformerPlugin) ApiPlugin
                    .create(RequestTransformerPlugin.class.getSimpleName());
    transformer.addBody("p1", "v1");
    transformer.addBody("p2", "v2");
    transformer.addHeader("h1", "v1");
    transformer.addHeader("h2", "v2");

    transformer.replaceBody("p3", "v3");
    transformer.replaceBody("p4", "v4");
    transformer.replaceHeader("h3", "v3");
    transformer.replaceHeader("h4", "v4");

    transformer.removeBody("p5");
    transformer.removeBody("p6");
    transformer.removeHeader("h5");
    transformer.removeHeader("h6");

    JsonObject jsonObject = plugin.encode();
    Assert.assertTrue(jsonObject.containsKey("response_transformer"));
  }

}
