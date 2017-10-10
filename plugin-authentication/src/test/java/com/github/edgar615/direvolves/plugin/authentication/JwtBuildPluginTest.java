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
public class JwtBuildPluginTest {
  @Test
  public void testDecode() {
    JsonObject config = new JsonObject()
            .put("jwt.build", true);
    ApiPluginFactory factory = new JwtBuildPluginFactory();
    JwtBuildPlugin plugin = (JwtBuildPlugin) factory.decode(config);
    Assert.assertNotNull(plugin);
  }

  @Test
  public void testEncode() {
    JwtBuildPlugin plugin = (JwtBuildPlugin) ApiPlugin.create(JwtBuildPlugin
                                                                      .class
                                                                      .getSimpleName());

    JsonObject jsonObject = plugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.getBoolean("jwt.build"));
  }

}
