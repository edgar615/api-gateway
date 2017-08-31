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
public class AppKeyUpdatePluginTest {
  @Test
  public void testDecode() {
    JsonObject config = new JsonObject()
            .put("appkey_update", true);
    ApiPluginFactory factory = new AppKeyUpdatePluginFactory();
    AppKeyUpdatePlugin plugin = (AppKeyUpdatePlugin) factory.decode(config);
    Assert.assertNotNull(plugin);
  }

  @Test
  public void testEncode() {
    AppKeyUpdatePlugin plugin = (AppKeyUpdatePlugin) ApiPlugin.create(AppKeyUpdatePlugin
                                                                      .class
                                                                      .getSimpleName());

    JsonObject jsonObject = plugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.getBoolean("appkey_update"));
  }

}
