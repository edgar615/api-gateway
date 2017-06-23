package com.edgar.direwolves.plugin.arg;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.util.validation.Rule;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by edgar on 16-10-22.
 */
public class UrlArgPluginTest {

  @Test
  public void testDecode() {
    JsonObject jsonObject = new JsonObject();
    JsonObject arg1 = new JsonObject()
            .put("name", "macAddress")
            .put("default_value", "FFFFFFFFFFFF")
            .put("rules", new JsonObject().put("required", true)
                    .put("regex", "[0-9A-F]{16}"));
    JsonObject arg2 = new JsonObject()
            .put("name", "type")
            .put("rules", new JsonObject().put("required", true)
                    .put("integer", true));
    JsonObject arg3 = new JsonObject()
            .put("name", "barcode");
    JsonArray urlArgs = new JsonArray()
            .add(arg1).add(arg2).add(arg3);
    jsonObject.put("url.arg", urlArgs);
    UrlArgPlugin plugin = (UrlArgPlugin) new UrlArgPluginFactory().decode(jsonObject);
    Assert.assertEquals(3, plugin.parameters().size());
  }

  @Test
  public void testEncode() {
    UrlArgPlugin urlArgPlugin = (UrlArgPlugin) ApiPlugin.create(UrlArgPlugin.class.getSimpleName());
    urlArgPlugin.add(Parameter.create("macAddress", "FFFFFFFFFFFF")
                             .addRule(Rule.regex("[0-9A-F]{16}")));
    urlArgPlugin.add(Parameter.create("type", null)
                             .addRule(Rule.required())
                             .addRule(Rule.integer()));
    urlArgPlugin.add(Parameter.create("barcode", null));
    JsonObject jsonObject = urlArgPlugin.encode();
    Assert.assertTrue(jsonObject.containsKey("url.arg"));
    JsonArray jsonArray = jsonObject.getJsonArray("url.arg");
    Assert.assertEquals(3, jsonArray.size());
    System.out.println(jsonArray);
  }

  @Test
  public void testRemoveParamter() {
    UrlArgPlugin urlArgPlugin = (UrlArgPlugin) ApiPlugin.create(UrlArgPlugin.class.getSimpleName());
    urlArgPlugin.add(Parameter.create("macAddress", "FFFFFFFFFFFF")
                             .addRule(Rule.regex("[0-9A-F]{16}")));
    urlArgPlugin.add(Parameter.create("type", null)
                             .addRule(Rule.required())
                             .addRule(Rule.integer()));
    urlArgPlugin.add(Parameter.create("barcode", null));

    Assert.assertEquals(3, urlArgPlugin.parameters().size());
    urlArgPlugin.remove("type");
    Assert.assertEquals(2, urlArgPlugin.parameters().size());
  }

  @Test
  public void testClearParamter() {
    UrlArgPlugin urlArgPlugin = (UrlArgPlugin) ApiPlugin.create(UrlArgPlugin.class.getSimpleName());
    urlArgPlugin.add(Parameter.create("macAddress", "FFFFFFFFFFFF")
                             .addRule(Rule.regex("[0-9A-F]{16}")));
    urlArgPlugin.add(Parameter.create("type", null)
                             .addRule(Rule.required())
                             .addRule(Rule.integer()));
    urlArgPlugin.add(Parameter.create("barcode", null));

    Assert.assertEquals(3, urlArgPlugin.parameters().size());
    urlArgPlugin.clear();
    Assert.assertEquals(0, urlArgPlugin.parameters().size());
  }
}
