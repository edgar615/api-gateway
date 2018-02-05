package com.github.edgar615.direvolves.plugin.auth;

import com.google.common.collect.Lists;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by edgar on 17-1-21.
 */
public class PermissionScopeCmdTest {

  ApiDefinition definition;

  PermissionScopeCmd cmd;

  @Before
  public void setUp() {
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                    80, "localhost");

    definition = ApiDefinition
        .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));

    definition.addPlugin(PermissionPlugin.create("device:get"));

    cmd = new PermissionScopeCmd();
  }

  @Test
  public void testDeleteSuccess() {
    JsonObject jsonObject = new JsonObject()
        .put("permission", "device:get");
    Assert.assertEquals(1, definition.plugins().size());

    cmd.handle(definition, jsonObject);
    Assert.assertEquals(0, definition.plugins().size());

  }

}
