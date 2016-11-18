package com.edgar.direwolves.plugin;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import com.edgar.direwolves.plugin.acl.AclRestrictionFactory;
import com.edgar.direwolves.plugin.acl.AclRestrictionPlugin;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public class AclRestrictionPluginTest {

  @Test
  public void testDecode() {
    JsonObject config = new JsonObject()
            .put("whitelist", new JsonArray().add("super").add("admin"))
            .put("blacklist", new JsonArray().add("user"));
    JsonObject jsonObject = new JsonObject()
            .put("acl_restriction", config);
    ApiPluginFactory<AclRestrictionPlugin> factory = new AclRestrictionFactory();
    AclRestrictionPlugin acl = factory.decode(jsonObject);
    Assert.assertEquals(2, acl.whitelist().size());
    Assert.assertEquals(1, acl.blacklist().size());
  }

  @Test
  public void testEncode() {
    ApiPlugin plugin = ApiPlugin.create(AclRestrictionPlugin.class.getSimpleName());
    AclRestrictionPlugin acl = (AclRestrictionPlugin) plugin;
    acl.addWhitelist("user")
            .addBlacklist("super")
            .addBlacklist("admin");
    Assert.assertEquals(2, acl.blacklist().size());
    Assert.assertEquals(1, acl.whitelist().size());

    JsonObject jsonObject = acl.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("acl_restriction"));
    JsonObject config = jsonObject.getJsonObject("acl_restriction");
    JsonArray blacklist = config.getJsonArray("blacklist");
    JsonArray whitelist = config.getJsonArray("whitelist");
    Assert.assertEquals(2, blacklist.size());
    Assert.assertEquals(1, whitelist.size());
  }

  @Test
  public void testRemove() {
    ApiPlugin plugin = ApiPlugin.create(AclRestrictionPlugin.class.getSimpleName());
    AclRestrictionPlugin acl = (AclRestrictionPlugin) plugin;
    acl.addWhitelist("user")
            .addBlacklist("super")
            .addBlacklist("admin");
    Assert.assertEquals(2, acl.blacklist().size());
    Assert.assertEquals(1, acl.whitelist().size());

    acl.removeWhitelist("user")
            .removeBlacklist("super");
    Assert.assertEquals(1, acl.blacklist().size());
    Assert.assertEquals(0, acl.whitelist().size());
  }

  @Test
  public void testRemoveAll() {
    ApiPlugin plugin = ApiPlugin.create(AclRestrictionPlugin.class.getSimpleName());
    AclRestrictionPlugin acl = (AclRestrictionPlugin) plugin;
    acl.addWhitelist("user")
            .addBlacklist("super")
            .addBlacklist("admin");
    Assert.assertEquals(2, acl.blacklist().size());
    Assert.assertEquals(1, acl.whitelist().size());

    acl.clearBlacklist()
            .clearWhitelist();
    Assert.assertEquals(0, acl.blacklist().size());
    Assert.assertEquals(0, acl.whitelist().size());
  }
}
