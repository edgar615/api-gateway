package com.edgar.direwolves.definition;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Edgar on 2016/9/13.
 *
 * @author Edgar  Date 2016/9/13
 */
public class JsonToApiDefinitionTest {

    @Test
    public void testJson() {
        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");
        ApiDefinition apiDefinition =  JsonToApiDefinition.instance().apply(addDeviceJson);
        Assert.assertEquals("/devices", apiDefinition.path());
    }

    @Test
    public void testJson2() {
        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/ip_device_add.json");
        ApiDefinition apiDefinition =  JsonToApiDefinition.instance().apply(addDeviceJson);
        Assert.assertNull(apiDefinition);
    }

}
