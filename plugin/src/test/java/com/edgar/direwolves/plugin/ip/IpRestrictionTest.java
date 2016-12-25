package com.edgar.direwolves.plugin.ip;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public class IpRestrictionTest {

  @Test
  public void testDecode() {
    JsonObject jsonObject = new JsonObject()
        .put("whitelist", new JsonArray().add("192.168.1.*").add("10.4.7.12"))
        .put("blacklist", new JsonArray().add("127.0.0.1"));
    ApiPluginFactory<IpRestriction> factory = new IpRestrictionFactory();
    IpRestriction ip = factory.decode(new JsonObject()
        .put("ip_restriction", jsonObject));
    Assert.assertEquals(2, ip.whitelist().size());
    Assert.assertEquals(1, ip.blacklist().size());
  }

  @Test
  public void testEncode() {
    ApiPlugin plugin = ApiPlugin.create(IpRestriction
        .class.getSimpleName());
    IpRestriction ip = (IpRestriction) plugin;
    ip.addWhitelist("127.0.0.1")
        .addBlacklist("192.168.1.*")
        .addBlacklist("10.4.7.12");
    Assert.assertEquals(2, ip.blacklist().size());
    Assert.assertEquals(1, ip.whitelist().size());

    JsonObject jsonObject = ip.encode();
    System.out.println(jsonObject);
    Assert.assertTrue(jsonObject.containsKey("ip_restriction"));
    JsonObject config = jsonObject.getJsonObject("ip_restriction");
    JsonArray blacklist = config.getJsonArray("blacklist");
    JsonArray whitelist = config.getJsonArray("whitelist");
    Assert.assertEquals(2, blacklist.size());
    Assert.assertEquals(1, whitelist.size());
  }

  @Test
  public void testRemove() {
    ApiPlugin plugin = ApiPlugin.create(IpRestriction
        .class.getSimpleName());
    IpRestriction ip = (IpRestriction) plugin;
    ip.addWhitelist("127.0.0.1")
        .addBlacklist("192.168.1.*")
        .addBlacklist("10.4.7.12");
    Assert.assertEquals(2, ip.blacklist().size());
    Assert.assertEquals(1, ip.whitelist().size());

    ip.removeWhitelist("127.0.0.1")
        .removeBlacklist("192.168.1.*");
    Assert.assertEquals(1, ip.blacklist().size());
    Assert.assertEquals(0, ip.whitelist().size());
  }

  @Test
  public void testRemoveAll() {
    ApiPlugin plugin = ApiPlugin.create(IpRestriction
        .class.getSimpleName());
    IpRestriction ip = (IpRestriction) plugin;
    ip.addWhitelist("127.0.0.1")
        .addBlacklist("192.168.1.*")
        .addBlacklist("10.4.7.12");
    Assert.assertEquals(2, ip.blacklist().size());
    Assert.assertEquals(1, ip.whitelist().size());

    ip.clearBlacklist()
        .clearWhitelist();
    Assert.assertEquals(0, ip.blacklist().size());
    Assert.assertEquals(0, ip.whitelist().size());
  }
}
