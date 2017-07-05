package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.plugin.ip.IpRestriction;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.vertx.eventbus.Event;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/1/21.
 *
 * @author Edgar  Date 2017/1/21
 */
@RunWith(VertxUnitRunner.class)
public class ApiPluginCmdTest extends BaseApiCmdTest {

  @Before
  public void setUp() {
    super.setUp();
    addMockApi();
//    cmd = new ApiPluginCmdFactory().create(Vertx.vertx(), new JsonObject());
  }

  @Test
  public void testAddIpBlacklist(TestContext testContext) {
//    Assert.assertEquals(2, registry.filter(null).size());
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "add_device")
            .put("subcmd", "ip.blacklist.add")
            .put("ip", "192.168.1.100");

    AtomicBoolean check1 = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.plugin")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.plugin", event, ar -> {
      if (ar.succeeded()) {
        System.out.println(ar.result());
        check1.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check2 = new AtomicBoolean();
    jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "add_device");
    event = Event.builder()
            .setAddress("direwolves.eb.api.get")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.get", event, ar -> {
      if (ar.succeeded()) {
        ApiDefinition apiDefinition =ApiDefinition.fromJson(ar.result().body().body());
        IpRestriction ipRestriction = (IpRestriction) apiDefinition.plugin(IpRestriction.class
                                                                                   .getSimpleName());
        testContext.assertNotNull(ipRestriction);
        testContext.assertEquals(1, ipRestriction.blacklist().size());
        testContext.assertEquals(0, ipRestriction.whitelist().size());
        check2.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();

      }
    });
    Awaitility.await().until(() -> check2.get());


    AtomicBoolean check3 = new AtomicBoolean();
    jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "get_device");
    event = Event.builder()
            .setAddress("direwolves.eb.api.get")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.get", event, ar -> {
      if (ar.succeeded()) {
        ApiDefinition apiDefinition =ApiDefinition.fromJson(ar.result().body().body());
        IpRestriction ipRestriction = (IpRestriction) apiDefinition.plugin(IpRestriction.class
                                                                                   .getSimpleName());
        testContext.assertNull(ipRestriction);
        check3.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();

      }
    });
    Awaitility.await().until(() -> check3.get());
  }

}
