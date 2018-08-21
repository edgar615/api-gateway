package com.github.edgar615.gateway.plugin.auth;

import com.google.common.collect.Lists;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.util.validation.ValidationException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by edgar on 17-1-21.
 */
public class AddPermissionCmdTest {

    ApiDefinition definition;

    AddPermissionCmd cmd;

    @Before
    public void setUp() {
        SimpleHttpEndpoint httpEndpoint =
                SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                        80, "localhost");

        definition = ApiDefinition
                .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));

        cmd = new AddPermissionCmd();
    }

    @Test
    public void testAddSuccess() {
        JsonObject jsonObject = new JsonObject()
                .put("permission", "device:get");

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
