package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public class AppKeyPluginTest {
  @Test
  public void testDecode() {
    JsonObject config = new JsonObject()
            .put("appkey", true);
    ApiPluginFactory factory = new AppKeyPluginFactory();
    AppKeyPlugin plugin = (AppKeyPlugin) factory.decode(config);
    Assert.assertNotNull(plugin);
//    Assert.assertEquals(2, plugin.authentications().size());
  }

  @Test
  public void testEncode() {
    AppKeyPlugin plugin = (AppKeyPlugin) ApiPlugin.create(AppKeyPlugin
                                                                  .class
                                                                  .getSimpleName());

    JsonObject jsonObject = plugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("appkey"));
    Assert.assertTrue(jsonObject.getBoolean("appkey", false));
  }

}
