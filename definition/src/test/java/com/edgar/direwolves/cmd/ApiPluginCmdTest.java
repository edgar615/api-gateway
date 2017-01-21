package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.plugin.ip.IpRestriction;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2017/1/21.
 *
 * @author Edgar  Date 2017/1/21
 */
@RunWith(VertxUnitRunner.class)
public class ApiPluginCmdTest {
  ApiDefinitionRegistry registry = ApiDefinitionRegistry.create();

  ApiCmd cmd;

  @Before
  public void setUp() {
    cmd = new ApiPluginCmdFactory().create(Vertx.vertx(), new JsonObject());

    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device")
            .put("method", "POST")
            .put("path", "/devices");
    JsonArray endpoints = new JsonArray()
            .add(new JsonObject().put("type", "http")
                         .put("name", "add_device")
                         .put("service", "device")
                         .put("method", "POST")
                         .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);
    jsonObject.put("authentication", true);

    registry.add(ApiDefinition.fromJson(jsonObject));

    jsonObject = new JsonObject()
            .put("name", "update_device")
            .put("method", "PUT")
            .put("path", "/devices");
    jsonObject.put("endpoints", endpoints);
    registry.add(ApiDefinition.fromJson(jsonObject));
  }

  @After
  public void tearDown() {
    registry.remove(null);
  }

  @Test
  public void testAddIpBlacklist(TestContext testContext) {
    Assert.assertEquals(2, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device")
            .put("subcmd", "ip.blacklist.add")
            .put("ip", "192.168.1.100");

    Async async = testContext.async();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                ApiDefinition apiDefinition = registry.filter("add_device").get(0);
                IpRestriction ipRestriction = (IpRestriction) apiDefinition.plugin(IpRestriction.class
                                                                           .getSimpleName());
                testContext.assertNotNull(ipRestriction);
                testContext.assertEquals(1, ipRestriction.blacklist().size());
                testContext.assertEquals(0, ipRestriction.whitelist().size());

                 apiDefinition = registry.filter("update_device").get(0);
                 ipRestriction = (IpRestriction) apiDefinition.plugin(IpRestriction.class
                                                                                           .getSimpleName());
                testContext.assertNull(ipRestriction);
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

}
