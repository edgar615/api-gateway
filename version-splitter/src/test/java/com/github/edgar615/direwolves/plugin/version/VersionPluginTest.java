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
public class VersionPluginTest {

  @Test
  public void testDecode() {
    JsonObject jsonObject = new JsonObject();
    ApiPluginFactory factory = new VersionPluginFactory();
    VersionPlugin plugin = (VersionPlugin) factory.decode(jsonObject);
    Assert.assertNull(plugin);

     jsonObject = new JsonObject()
            .put("version", "20170102");
    plugin = (VersionPlugin) factory.decode(jsonObject);
    Assert.assertEquals("20170102", plugin.version());
  }

  @Test
  public void testEncode() {
    ApiPlugin plugin = ApiPlugin.create(VersionPlugin.class.getSimpleName());
    VersionPlugin versionPlugin = (VersionPlugin) plugin;
    versionPlugin.setVersion("test");

    JsonObject jsonObject = versionPlugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("version"));
    Assert.assertEquals("test", jsonObject.getString("version"));
  }
}
