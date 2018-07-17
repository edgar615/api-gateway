package com.github.edgar615.gateway.plugin.user;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public class UserRestrictionTest {

  @Test
  public void testDecode() {
    JsonObject config = new JsonObject()
            .put("whitelist", new JsonArray().add(1).add("2"))
            .put("blacklist", new JsonArray().add(3));
    JsonObject jsonObject = new JsonObject()
            .put("user.restriction", config);
    ApiPluginFactory factory = new UserRestrictionFactory();
    UserRestriction plugin = (UserRestriction) factory.decode(jsonObject);
    Assert.assertEquals(2, plugin.whitelist().size());
    Assert.assertEquals(1, plugin.blacklist().size());
  }

  @Test
  public void testEncode() {
    ApiPlugin plugin = ApiPlugin.create(UserRestriction.class.getSimpleName());
    UserRestriction restrictionPlugin = (UserRestriction) plugin;
    restrictionPlugin.addWhitelist("1")
            .addBlacklist("2")
            .addBlacklist("3");
    Assert.assertEquals(2, restrictionPlugin.blacklist().size());
    Assert.assertEquals(1, restrictionPlugin.whitelist().size());

    JsonObject jsonObject = restrictionPlugin.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("user.restriction"));
    JsonObject config = jsonObject.getJsonObject("user.restriction");
    JsonArray blacklist = config.getJsonArray("blacklist");
    JsonArray whitelist = config.getJsonArray("whitelist");
    Assert.assertEquals(2, blacklist.size());
    Assert.assertEquals(1, whitelist.size());
  }

  @Test
  public void testRemove() {
    ApiPlugin plugin = ApiPlugin.create(UserRestriction.class.getSimpleName());
    UserRestriction restrictionPlugin = (UserRestriction) plugin;
    restrictionPlugin.addWhitelist("1")
            .addBlacklist("2")
            .addBlacklist("3");
    Assert.assertEquals(1, restrictionPlugin.whitelist().size());

    restrictionPlugin.removeWhitelist("1")
            .removeBlacklist("2");
    Assert.assertEquals(1, restrictionPlugin.blacklist().size());
    Assert.assertEquals(0, restrictionPlugin.whitelist().size());
  }

  @Test
  public void testRemoveAll() {
    ApiPlugin plugin = ApiPlugin.create(UserRestriction.class.getSimpleName());
    UserRestriction restrictionPlugin = (UserRestriction) plugin;
    restrictionPlugin.addWhitelist("1")
            .addBlacklist("2")
            .addBlacklist("3");
    Assert.assertEquals(2, restrictionPlugin.blacklist().size());
    Assert.assertEquals(1, restrictionPlugin.whitelist().size());

    restrictionPlugin.clearBlacklist()
            .clearWhitelist();
    Assert.assertEquals(0, restrictionPlugin.blacklist().size());
    Assert.assertEquals(0, restrictionPlugin.whitelist().size());
  }
}
