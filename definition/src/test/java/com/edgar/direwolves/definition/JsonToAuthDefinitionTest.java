package com.edgar.direwolves.definition;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by Edgar on 2016/9/13.
 *
 * @author Edgar  Date 2016/9/13
 */
public class JsonToAuthDefinitionTest {

    @Test
    public void testJson() {
        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");
        List<AuthDefinition> authDefinitions =  JsonToAuthDefinition.instance().apply(addDeviceJson);
        Assert.assertEquals(1, authDefinitions.size());
        Assert.assertEquals(AuthType.JWT, authDefinitions.get(0).authType());
    }

    @Test
    public void testJson2() {
        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/auth_of_device_add.json");
        List<AuthDefinition> authDefinitions =  JsonToAuthDefinition.instance().apply(addDeviceJson);
        Assert.assertEquals(2, authDefinitions.size());
        Assert.assertEquals(AuthType.JWT, authDefinitions.get(0).authType());
        Assert.assertEquals(AuthType.OAUTH, authDefinitions.get(1).authType());
    }

    @Test
    public void testJson3() {
        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add2.json");
        List<AuthDefinition> authDefinitions =  JsonToAuthDefinition.instance().apply(addDeviceJson);
        Assert.assertEquals(0, authDefinitions.size());
    }

}
