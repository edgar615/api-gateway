package com.edgar.direwolves.plugin.authorization;

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
 * Created by edgar on 17-1-21.
 */
public class AddAuthoriseCmdTest {

  ApiDefinition definition;

  AddAuthoriseCmd cmd;

  @Before
  public void setUp() {
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                    80, "localhost");

    definition = ApiDefinition
        .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));

    cmd = new AddAuthoriseCmd();
  }

  @Test
  public void testAddSuccess() {
    JsonObject jsonObject = new JsonObject()
        .put("scope", "device:get");

    Assert.assertEquals(0, definition.plugins().size());

    cmd.handle(definition, jsonObject);
    Assert.assertEquals(1, definition.plugins().size());

  }

  @Test
  public void missScopeShouldThrowValidationException() {
    JsonObject jsonObject = new JsonObject()
        .put("ip", "192.168.1.100");
    try {
      cmd.handle(definition, jsonObject);
      Assert.fail();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.assertTrue(e instanceof ValidationException);
    }

  }
}
