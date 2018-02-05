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
public class TokenPluginTest {
  @Test
  public void testDecode() {
    JsonObject config = new JsonObject()
            .put("token", true);
    ApiPluginFactory factory = new TokenPluginFactory();
    TokenPlugin plugin = (TokenPlugin) factory.decode(config);
    Assert.assertNotNull(plugin);
  }

  @Test
  public void testEncode() {
    TokenPlugin plugin = (TokenPlugin) ApiPlugin.create(TokenPlugin
                                                                .class
                                                                .getSimpleName());
    JsonObject jsonObject = plugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.getBoolean("token"));
  }

}
