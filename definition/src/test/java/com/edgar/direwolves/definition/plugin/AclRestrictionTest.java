package com.edgar.direwolves.definition.plugin;

import com.edgar.direwolves.plugin.acl.AclRestriction;
import com.edgar.direwolves.plugin.acl.AclRestrictionFactory;
import com.edgar.direwolves.plugin.ApiPlugin;
import com.edgar.direwolves.plugin.ApiPluginFactory;
import com.edgar.direwolves.plugin.ip.IpRestriction;
import com.edgar.direwolves.plugin.ip.IpRestrictionFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public class AclRestrictionTest {

  @Test
  public void testDecode() {
    JsonObject jsonObject = new JsonObject()
            .put("name", "acl_restriction")
            .put("whitelist", new JsonArray().add("super").add("admin"))
            .put("blacklist", new JsonArray().add("user"));
    ApiPluginFactory<AclRestriction> factory = new AclRestrictionFactory();
    AclRestriction acl = factory.decode(jsonObject);
    Assert.assertEquals(2, acl.whitelist().size());
    Assert.assertEquals(1, acl.blacklist().size());
  }

  @Test
  public void testEncode() {
    ApiPlugin plugin = ApiPlugin.create("acl_restriction");
    AclRestriction acl = (AclRestriction) plugin;
    acl.addWhitelist("user")
            .addBlacklist("super")
            .addBlacklist("admin");
    Assert.assertEquals(2, acl.blacklist().size());
    Assert.assertEquals(1, acl.whitelist().size());

    ApiPluginFactory<AclRestriction> factory = new AclRestrictionFactory();
    JsonObject jsonObject = factory.encode(acl);
    System.out.println(jsonObject);
    Assert.assertEquals("acl_restriction", jsonObject.getString("name"));
    JsonArray blacklist = jsonObject.getJsonArray("blacklist");
    JsonArray whitelist = jsonObject.getJsonArray("whitelist");
    Assert.assertEquals(2, blacklist.size());
    Assert.assertEquals(1, whitelist.size());
  }

  @Test
  public void testRemove() {
    ApiPlugin plugin = ApiPlugin.create("acl_restriction");
    AclRestriction acl = (AclRestriction) plugin;
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
    ApiPlugin plugin = ApiPlugin.create("acl_restriction");
    AclRestriction acl = (AclRestriction) plugin;
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
