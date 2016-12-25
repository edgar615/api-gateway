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
public class AppKeyCheckerPluginTest {
  @Test
  public void testDecode() {
    JsonObject config = new JsonObject()
        .put("app_key_checker", true);
    ApiPluginFactory<AppKeyCheckerPlugin> factory = new AppKeyCheckerPluginFactory();
    AppKeyCheckerPlugin plugin = factory.decode(config);
    Assert.assertNotNull(plugin);
//    Assert.assertEquals(2, plugin.authentications().size());
  }

  @Test
  public void testEncode() {
    AppKeyCheckerPlugin plugin = (AppKeyCheckerPlugin) ApiPlugin.create(AppKeyCheckerPlugin
        .class
        .getSimpleName());

    JsonObject jsonObject = plugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("app_key_checker"));
    Assert.assertTrue(jsonObject.getBoolean("app_key_checker", false));
  }

}
