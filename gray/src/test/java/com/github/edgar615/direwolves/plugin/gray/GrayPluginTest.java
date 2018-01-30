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
    ApiPluginFactory factory = new ClientApiVersionPluginFactory();
    ClientApiVersionPlugin plugin = (ClientApiVersionPlugin) factory.decode(jsonObject);
    Assert.assertNull(plugin);

    jsonObject = new JsonObject()
            .put("ca.version", "20170102");
    plugin = (ClientApiVersionPlugin) factory.decode(jsonObject);
    Assert.assertNull(plugin);

    jsonObject = new JsonObject()
            .put("ca.version", "floor");
    plugin = (ClientApiVersionPlugin) factory.decode(jsonObject);
    Assert.assertEquals("floor", plugin.type());

    jsonObject = new JsonObject()
            .put("ca.version", "ceil");
    plugin = (ClientApiVersionPlugin) factory.decode(jsonObject);
    Assert.assertEquals("ceil", plugin.type());
  }

  @Test
  public void testEncode() {
    ApiPlugin plugin = ApiPlugin.create(ClientApiVersionPlugin.class.getSimpleName());
    ClientApiVersionPlugin versionPlugin = (ClientApiVersionPlugin) plugin;
    versionPlugin.floor();

    JsonObject jsonObject = versionPlugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("ca.version"));
    Assert.assertEquals("floor", jsonObject.getString("ca.version"));
  }
}
