package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public class AuthenticationPluginTest {
  @Test
  public void testDecode() {
    JsonObject config = new JsonObject()
        .put("authentication", new JsonArray().add("jwt").add("oauth2").add("jwt"));
    ApiPluginFactory<AuthenticationPlugin> factory = new AuthenticationPluginFactory();
    AuthenticationPlugin plugin = factory.decode(config);
    Assert.assertEquals(2, plugin.authentications().size());
  }

  @Test
  public void testEncode() {
    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
        .class
        .getSimpleName());
    plugin.add("jwt")
        .add("oauth2")
        .add("jwt");
    Assert.assertEquals(2, plugin.authentications().size());

    JsonObject jsonObject = plugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("authentication"));
    JsonArray auth = jsonObject.getJsonArray("authentication");
    Assert.assertEquals(2, auth.size());
  }

  @Test
  public void testRemove() {
    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
        .class
        .getSimpleName());
    plugin.add("jwt")
        .add("oauth2")
        .add("jwt");
    Assert.assertEquals(2, plugin.authentications().size());

    plugin.remove("user")
        .remove("jwt");
    Assert.assertEquals(1, plugin.authentications().size());
  }

  @Test
  public void testRemoveAll() {
    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
        .class
        .getSimpleName());
    plugin.add("jwt")
        .add("oauth2")
        .add("jwt");
    Assert.assertEquals(2, plugin.authentications().size());

    plugin.clear();
    Assert.assertEquals(0, plugin.authentications().size());
  }
}
