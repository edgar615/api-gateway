package com.github.edgar615.direvolves.plugin.authentication;

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
public class JwtCleanPluginTest {
  @Test
  public void testDecode() {
    JsonObject config = new JsonObject()
            .put("jwt.clean", true);
    ApiPluginFactory factory = new JwtCleanPluginFactory();
    JwtCleanPlugin plugin = (JwtCleanPlugin) factory.decode(config);
    Assert.assertNotNull(plugin);
  }

  @Test
  public void testEncode() {
    JwtCleanPlugin plugin = (JwtCleanPlugin) ApiPlugin.create(JwtCleanPlugin
                                                                      .class
                                                                      .getSimpleName());

    JsonObject jsonObject = plugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.getBoolean("jwt.clean"));
  }

}
