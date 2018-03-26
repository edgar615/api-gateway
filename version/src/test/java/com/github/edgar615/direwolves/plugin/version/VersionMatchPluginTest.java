package com.github.edgar615.direwolves.plugin.version;

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
public class VersionMatchPluginTest {

  @Test
  public void testDecode() {
    JsonObject jsonObject = new JsonObject();
    ApiPluginFactory factory = new VersionMatchPluginFactory();
    VersionMatchPlugin plugin = (VersionMatchPlugin) factory.decode(jsonObject);
    Assert.assertNull(plugin);

    jsonObject = new JsonObject()
            .put("version.match", "20170102");
    plugin = (VersionMatchPlugin) factory.decode(jsonObject);
    Assert.assertNull(plugin);

    jsonObject = new JsonObject()
            .put("version.match", "floor");
    plugin = (VersionMatchPlugin) factory.decode(jsonObject);
    Assert.assertEquals("floor", plugin.type());

    jsonObject = new JsonObject()
            .put("version.match", "ceil");
    plugin = (VersionMatchPlugin) factory.decode(jsonObject);
    Assert.assertEquals("ceil", plugin.type());
  }

  @Test
  public void testEncode() {
    ApiPlugin plugin = ApiPlugin.create(VersionMatchPlugin.class.getSimpleName());
    VersionMatchPlugin versionPlugin = (VersionMatchPlugin) plugin;
    versionPlugin.floor();

    JsonObject jsonObject = versionPlugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("version.match"));
    Assert.assertEquals("floor", jsonObject.getString("version.match"));
  }
}
