package com.github.edgar615.gateway.core.definition;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/9/13.
 *
 * @author Edgar  Date 2016/9/13
 */
public class RegexApiDefinitionDecoderTest {

    @Test
    public void testRegex() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("path", "/devices/([\\d+]+)")
                .put("type", "regex");
        JsonArray endpoints = new JsonArray();
        endpoints.add(new JsonObject()
                              .put("type", "simple-http").put("host", "localhost").put("port", 80)
                              .put("name", "device.add.1.2.0")
                              .put("host", "localhost")
                              .put("port", 80)
                              .put("path", "/devices/add"));
        jsonObject.put("endpoints", endpoints);
        ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
        Assert.assertEquals(HttpMethod.GET, apiDefinition.method());
        Assert.assertEquals("/devices/([\\d+]+)", apiDefinition.path());
        Assert.assertEquals(1, apiDefinition.endpoints().size());
        Endpoint endpoint = apiDefinition.endpoints().get(0);
        Assert.assertTrue(endpoint instanceof SimpleHttpEndpoint);
        SimpleHttpEndpoint httpEndpoint = (SimpleHttpEndpoint) endpoint;
        Assert.assertEquals("/devices/add", httpEndpoint.path());
        Assert.assertEquals("device.add.1.2.0", httpEndpoint.name());
        Assert.assertEquals(HttpMethod.GET, httpEndpoint.method());

        Assert.assertEquals(0, apiDefinition.plugins().size());

        Assert.assertTrue(apiDefinition instanceof RegexPathApiDefinition);
    }

    @Test
    public void testAnt2() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("path", "/devices")
                .put("type", "regex");
        JsonArray endpoints = new JsonArray();
        endpoints.add(new JsonObject()
                              .put("type", "simple-http").put("host", "localhost").put("port", 80)
                              .put("name", "device.add.1.2.0")
                              .put("host", "localhost")
                              .put("port", 80)
                              .put("path", "/devices/add"));
        jsonObject.put("endpoints", endpoints);
        ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
        Assert.assertEquals(HttpMethod.GET, apiDefinition.method());
        Assert.assertEquals("/devices", apiDefinition.path());
        Assert.assertEquals(1, apiDefinition.endpoints().size());
        Endpoint endpoint = apiDefinition.endpoints().get(0);
        Assert.assertTrue(endpoint instanceof SimpleHttpEndpoint);
        SimpleHttpEndpoint httpEndpoint = (SimpleHttpEndpoint) endpoint;
        Assert.assertEquals("/devices/add", httpEndpoint.path());
        Assert.assertEquals("device.add.1.2.0", httpEndpoint.name());
        Assert.assertEquals(HttpMethod.GET, httpEndpoint.method());

        Assert.assertEquals(0, apiDefinition.plugins().size());

        Assert.assertTrue(apiDefinition instanceof RegexPathApiDefinition);
    }
}