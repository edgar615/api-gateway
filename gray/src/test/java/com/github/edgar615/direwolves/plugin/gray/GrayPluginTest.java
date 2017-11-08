package com.github.edgar615.direwolves.plugin.gray;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public class GrayPluginTest {

  @Test
  public void testDecode() {
    JsonObject jsonObject = new JsonObject();
    ApiPluginFactory factory = new HeaderGrayPluginFactory();
    HeaderGrayPlugin plugin = (HeaderGrayPlugin) factory.decode(jsonObject);
    Assert.assertNull(plugin);

    jsonObject = new JsonObject()
            .put("gray.header", "20170102");
    plugin = (HeaderGrayPlugin) factory.decode(jsonObject);
    Assert.assertNull(plugin);

    jsonObject = new JsonObject()
            .put("gray.header", "floor");
    plugin = (HeaderGrayPlugin) factory.decode(jsonObject);
    Assert.assertEquals("floor", plugin.type());

    jsonObject = new JsonObject()
            .put("gray.header", "ceil");
    plugin = (HeaderGrayPlugin) factory.decode(jsonObject);
    Assert.assertEquals("ceil", plugin.type());
  }

  @Test
  public void testEncode() {
    ApiPlugin plugin = ApiPlugin.create(HeaderGrayPlugin.class.getSimpleName());
    HeaderGrayPlugin versionPlugin = (HeaderGrayPlugin) plugin;
    versionPlugin.floor();

    JsonObject jsonObject = versionPlugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("gray.header"));
    Assert.assertEquals("floor", jsonObject.getString("gray.header"));
  }
}
