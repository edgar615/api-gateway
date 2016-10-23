package com.edgar.direwolves.plugin;

import com.edgar.direwolves.plugin.transformer.ResponseTransformer;
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
    JsonArray jsonArray = new JsonArray();
    JsonObject request = new JsonObject()
        .put("name", "add_device")
        .put("header.remove", new JsonArray().add("h3").add("h4"))
        .put("body.remove", new JsonArray().add("p3").add("p4"))
        .put("header.replace", new JsonArray().add("h5:v2").add("h6:v1"))
        .put("body.replace", new JsonArray().add("p5:v2").add("p6:v1"))
        .put("header.add", new JsonArray().add("h1:v2").add("h2:v1"))
        .put("body.add", new JsonArray().add("p1:v2").add("p2:v1"));
    jsonArray.add(request);
    ResponseTransformerPlugin plugin = new ResponseTransformerPluginFactory().decode(new JsonObject().put("response_transformer", jsonArray));
    ResponseTransformer transformer = plugin.transformer("add_device");
    Assert.assertEquals("add_device", transformer.name());
    Assert.assertEquals(2, transformer.headerRemoved().size());
    Assert.assertEquals(2, transformer.bodyRemoved().size());
    Assert.assertEquals(2, transformer.headerReplaced().size());
    Assert.assertEquals(2, transformer.bodyReplaced().size());
    Assert.assertEquals(2, transformer.headerAdded().size());
    Assert.assertEquals(2, transformer.bodyAdded().size());
  }

  @Test
  public void testEncode() {
    ResponseTransformerPlugin plugin = (ResponseTransformerPlugin) ApiPlugin.create("response_transformer");
    ResponseTransformer transformer = ResponseTransformer.create("add_device");
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

    plugin.addTransformer(transformer);

    JsonObject jsonObject = plugin.encode();
    Assert.assertTrue(jsonObject.containsKey("response_transformer"));
  }

  @Test
  public void testRemove() {
    ResponseTransformerPlugin plugin = (ResponseTransformerPlugin) ApiPlugin.create("response_transformer");
    ResponseTransformer transformer = ResponseTransformer.create("add_device");
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

    plugin.addTransformer(transformer);

    transformer = ResponseTransformer.create("add_device2");
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

    plugin.addTransformer(transformer);


    Assert.assertEquals(2, plugin.transformers().size());
    plugin.removeTransformer("add_device");
    Assert.assertEquals(1, plugin.transformers().size());
  }

  @Test
  public void testClear() {
    ResponseTransformerPlugin plugin = (ResponseTransformerPlugin) ApiPlugin.create("response_transformer");
    ResponseTransformer transformer = ResponseTransformer.create("add_device");
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

    plugin.addTransformer(transformer);
    Assert.assertEquals(1, plugin.transformers().size());
    plugin.clear();
    Assert.assertEquals(0, plugin.transformers().size());
  }
}
