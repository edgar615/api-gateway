package com.edgar.direwolves.definition.plugin;

import com.edgar.direwolves.plugin.ApiPlugin;
import com.edgar.direwolves.plugin.ApiPluginFactory;
import com.edgar.direwolves.plugin.ip.IpRestriction;
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
public class IpRestrictionTest {

  @Test
  public void testDecode() {
    JsonObject jsonObject = new JsonObject()
            .put("name", "ip_restriction")
            .put("whitelist", new JsonArray().add("192.168.1.*").add("10.4.7.12"))
            .put("blacklist", new JsonArray().add("127.0.0.1"));
    ApiPluginFactory<IpRestriction> factory = new IpRestrictionFactory();
    IpRestriction ip = factory.decode(jsonObject);
    Assert.assertEquals(2, ip.whitelist().size());
    Assert.assertEquals(1, ip.blacklist().size());
  }

  @Test
  public void testEncode() {
    ApiPlugin plugin = ApiPlugin.create("ip_restriction");
    IpRestriction ip = (IpRestriction) plugin;
    ip.addWhitelist("127.0.0.1")
            .addBlacklist("192.168.1.*")
            .addBlacklist("10.4.7.12");
    Assert.assertEquals(2, ip.blacklist().size());
    Assert.assertEquals(1, ip.whitelist().size());

    ApiPluginFactory<IpRestriction> factory = new IpRestrictionFactory();
    JsonObject jsonObject = factory.encode(ip);
    System.out.println(jsonObject);
    Assert.assertEquals("ip_restriction", jsonObject.getString("name"));
    JsonArray blacklist = jsonObject.getJsonArray("blacklist");
    JsonArray whitelist = jsonObject.getJsonArray("whitelist");
    Assert.assertEquals(2, blacklist.size());
    Assert.assertEquals(1, whitelist.size());
  }

  @Test
  public void testRemove() {
    ApiPlugin plugin = ApiPlugin.create("ip_restriction");
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
    ApiPlugin plugin = ApiPlugin.create("ip_restriction");
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
