package com.edgar.direwolves.plugin.ip;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.definition.SimpleHttpEndpoint;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Edgar on 2017/1/21.
 *
 * @author Edgar  Date 2017/1/21
 */
public class DeleteBlacklistCmdTest {

  ApiDefinition definition;

  @Before
  public void setUp() {
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                    80, "localhost");

    definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
  }

  @Test
  public void undefinedPluginShouldSuccess() {
    IpRestriction restriction =
            (IpRestriction) definition.plugin(IpRestriction.class.getSimpleName());
    Assert.assertNull(restriction);

    DeleteBlacklistCmd cmd = new DeleteBlacklistCmd();
    JsonObject jsonObject = new JsonObject()
            .put("ip", "192.168.1.100");
    cmd.handle(definition, jsonObject);

    restriction =
            (IpRestriction) definition.plugin(IpRestriction.class.getSimpleName());
    Assert.assertNull(restriction);
  }

  @Test
  public void missIpShouldThrowValidationException() {
    IpRestriction restriction =
            (IpRestriction) definition.plugin(IpRestriction.class.getSimpleName());
    Assert.assertNull(restriction);

    DeleteBlacklistCmd cmd = new DeleteBlacklistCmd();
    JsonObject jsonObject = new JsonObject()
            .put("ip2", "192.168.1.100");
    try {
      cmd.handle(definition, jsonObject);
      Assert.fail();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.assertTrue(e instanceof ValidationException);
    }
  }

  @Test
  public void testDeleteSuccess() {
    IpRestriction restriction =
            (IpRestriction) definition.plugin(IpRestriction.class.getSimpleName());
    Assert.assertNull(restriction);

    AddBlacklistCmd addBlacklistCmd = new AddBlacklistCmd();
    JsonObject jsonObject = new JsonObject()
            .put("ip", "192.168.1.100");
    addBlacklistCmd.handle(definition, jsonObject);

    restriction =
            (IpRestriction) definition.plugin(IpRestriction.class.getSimpleName());
    Assert.assertNotNull(restriction);
    Assert.assertEquals(1, restriction.blacklist().size());

    DeleteBlacklistCmd deleteBlacklistCmd = new DeleteBlacklistCmd();
    deleteBlacklistCmd.handle(definition, jsonObject);

    restriction =
            (IpRestriction) definition.plugin(IpRestriction.class.getSimpleName());
    Assert.assertNotNull(restriction);
    Assert.assertEquals(0, restriction.blacklist().size());

  }

  @Test
  public void testDeleteUndefindedIpSuccess() {
    IpRestriction restriction =
            (IpRestriction) definition.plugin(IpRestriction.class.getSimpleName());
    Assert.assertNull(restriction);

    AddBlacklistCmd addBlacklistCmd = new AddBlacklistCmd();
    JsonObject jsonObject = new JsonObject()
            .put("ip", "192.168.1.100");
    addBlacklistCmd.handle(definition, jsonObject);

    restriction =
            (IpRestriction) definition.plugin(IpRestriction.class.getSimpleName());
    Assert.assertNotNull(restriction);
    Assert.assertEquals(1, restriction.blacklist().size());

    jsonObject.put("ip", "192.168.1.110");
    DeleteBlacklistCmd deleteBlacklistCmd = new DeleteBlacklistCmd();
    deleteBlacklistCmd.handle(definition, jsonObject);

    restriction =
            (IpRestriction) definition.plugin(IpRestriction.class.getSimpleName());
    Assert.assertNotNull(restriction);
    Assert.assertEquals(1, restriction.blacklist().size());

  }
}
