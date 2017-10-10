package com.github.edgar615.direwolves.plugin.authorization;

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
public class AuthorisePluginTest {
  @Test
  public void testDecode() {
    JsonObject config = new JsonObject()
            .put("scope", "user.read");
    ApiPluginFactory factory = new AuthorisePluginFactory();
    AuthorisePlugin plugin = (AuthorisePlugin) factory.decode(config);
    Assert.assertNotNull(plugin);
    Assert.assertEquals("user.read", plugin.scope());
  }

  @Test
  public void testEncode() {
    AuthorisePluginImpl plugin = (AuthorisePluginImpl) ApiPlugin.create(AuthorisePlugin
                                                                                .class
                                                                                .getSimpleName());
    plugin.setScope("user.read");
    JsonObject jsonObject = plugin.encode();
    System.out.println(jsonObject);
    Assert.assertEquals("user.read", jsonObject.getString("scope"));
  }

  @Test
  public void testNullShoudReturnEmptyJson() {
    ApiPluginFactory factory = new AuthorisePluginFactory();
    JsonObject jsonObject = factory.encode(null);
    Assert.assertTrue(jsonObject.isEmpty());
  }

}
