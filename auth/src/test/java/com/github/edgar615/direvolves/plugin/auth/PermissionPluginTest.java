package com.github.edgar615.direvolves.plugin.auth;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public class PermissionPluginTest {
  @Test
  public void testDecode() {
    JsonObject config = new JsonObject()
            .put("permission", "user.read");
    ApiPluginFactory factory = new PermissionPluginFactory();
    PermissionPlugin plugin = (PermissionPlugin) factory.decode(config);
    Assert.assertNotNull(plugin);
    Assert.assertEquals("user.read", plugin.permission());
  }

  @Test
  public void testEncode() {
    PermissionPluginImpl plugin = (PermissionPluginImpl) ApiPlugin.create(PermissionPlugin
                                                                                .class
                                                                                .getSimpleName());
    plugin.setPermission("user.read");
    JsonObject jsonObject = plugin.encode();
    System.out.println(jsonObject);
    Assert.assertEquals("user.read", jsonObject.getString("permission"));
  }

  @Test
  public void testNullShoudReturnEmptyJson() {
    ApiPluginFactory factory = new PermissionPluginFactory();
    JsonObject jsonObject = factory.encode(null);
    Assert.assertTrue(jsonObject.isEmpty());
  }

}
