package com.github.edgar615.gateway.core.definition;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by Edgar on 2016/9/13.
 *
 * @author Edgar  Date 2016/9/13
 */
public class ApiDefinitionDecoderTest {

    @Test
    public void testDefaultEndpointName() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("path", "/devices");
        JsonArray endpoints = new JsonArray();
        endpoints.add(new JsonObject()
                              .put("type", "simple-http").put("host", "localhost").put("port", 80)
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
        Assert.assertEquals("default", httpEndpoint.name());
        Assert.assertEquals(HttpMethod.GET, httpEndpoint.method());

        Assert.assertEquals(0, apiDefinition.plugins().size());

        Assert.assertFalse(apiDefinition instanceof AntPathApiDefinition);
    }

    @Test
    public void testMinimizeConfig() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("path", "/devices");
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

        Assert.assertFalse(apiDefinition instanceof AntPathApiDefinition);
    }

    @Test
    public void testTwoEndpoints() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("method", "post")
                .put("path", "/devices");
        JsonArray endpoints = new JsonArray();
        endpoints.add(new JsonObject()
                              .put("type", "simple-http").put("host", "localhost").put("port", 80)
                              .put("name", "device.add.1.2.0")
                              .put("host", "localhost")
                              .put("port", 80)
                              .put("method", "undefined")
                              .put("path", "/devices/add"))
                .add(new JsonObject()
                             .put("type", "simple-http").put("host", "localhost").put("port", 80)
                             .put("name", "device.delete.1.2.0")
                             .put("host", "localhost")
                             .put("port", 90)
                             .put("method", "delete")
                             .put("path", "/devices"));
        jsonObject.put("endpoints", endpoints);

        ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
        Assert.assertEquals("/devices", apiDefinition.path());
        Assert.assertEquals(HttpMethod.POST, apiDefinition.method());

        Assert.assertEquals(2, apiDefinition.endpoints().size());
        Endpoint endpoint = apiDefinition.endpoints().get(0);
        Assert.assertTrue(endpoint instanceof SimpleHttpEndpoint);
        SimpleHttpEndpoint httpEndpoint = (SimpleHttpEndpoint) endpoint;
        Assert.assertEquals("/devices/add", httpEndpoint.path());
        Assert.assertEquals("device.add.1.2.0", httpEndpoint.name());
        Assert.assertEquals(HttpMethod.GET, httpEndpoint.method());

        endpoint = apiDefinition.endpoints().get(1);
        Assert.assertTrue(endpoint instanceof SimpleHttpEndpoint);
        httpEndpoint = (SimpleHttpEndpoint) endpoint;
        Assert.assertEquals("/devices", httpEndpoint.path());
        Assert.assertEquals("device.delete.1.2.0", httpEndpoint.name());
        Assert.assertEquals(HttpMethod.DELETE, httpEndpoint.method());

        Assert.assertEquals(0, apiDefinition.plugins().size());
    }

    @Test
    public void testTwoEndpointsWithOneDefault() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("method", "post")
                .put("path", "/devices");
        JsonArray endpoints = new JsonArray();
        endpoints.add(new JsonObject()
                              .put("type", "simple-http").put("host", "localhost").put("port", 80)
                              .put("name", "device.add.1.2.0")
                              .put("host", "localhost")
                              .put("port", 80)
                              .put("method", "undefined")
                              .put("path", "/devices/add"))
                .add(new JsonObject()
                             .put("type", "simple-http").put("host", "localhost").put("port", 80)
                             .put("host", "localhost")
                             .put("port", 90)
                             .put("method", "delete")
                             .put("path", "/devices"));
        jsonObject.put("endpoints", endpoints);

        ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
        Assert.assertEquals("/devices", apiDefinition.path());
        Assert.assertEquals(HttpMethod.POST, apiDefinition.method());

        Assert.assertEquals(2, apiDefinition.endpoints().size());
        Endpoint endpoint = apiDefinition.endpoints().get(0);
        Assert.assertTrue(endpoint instanceof SimpleHttpEndpoint);
        SimpleHttpEndpoint httpEndpoint = (SimpleHttpEndpoint) endpoint;
        Assert.assertEquals("/devices/add", httpEndpoint.path());
        Assert.assertEquals("device.add.1.2.0", httpEndpoint.name());
        Assert.assertEquals(HttpMethod.GET, httpEndpoint.method());

        endpoint = apiDefinition.endpoints().get(1);
        Assert.assertTrue(endpoint instanceof SimpleHttpEndpoint);
        httpEndpoint = (SimpleHttpEndpoint) endpoint;
        Assert.assertEquals("/devices", httpEndpoint.path());
        Assert.assertEquals("default", httpEndpoint.name());
        Assert.assertEquals(HttpMethod.DELETE, httpEndpoint.method());

        Assert.assertEquals(0, apiDefinition.plugins().size());
    }


    @Test
    public void testUndefinedEndpoint() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("path", "/devices");
        JsonArray endpoints = new JsonArray();
        endpoints.add(new JsonObject()
                              .put("type", UUID.randomUUID().toString())
                              .put("name", "device.add.1.2.0")
                              .put("host", "localhost")
                              .put("port", 90)
                              .put("path", "/devices/add"));
        jsonObject.put("endpoints", endpoints);
        try {
            ApiDefinition.fromJson(jsonObject);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UnsupportedOperationException);
        }
    }

    @Test
    public void testTwoEndpointsWithTwoDefault() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("method", "post")
                .put("path", "/devices");
        JsonArray endpoints = new JsonArray();
        endpoints.add(new JsonObject()
                              .put("type", "simple-http").put("host", "localhost").put("port", 80)
                              .put("host", "localhost")
                              .put("port", 80)
                              .put("method", "undefined")
                              .put("path", "/devices/add"))
                .add(new JsonObject()
                             .put("type", "simple-http").put("host", "localhost").put("port", 80)
                             .put("host", "localhost")
                             .put("port", 90)
                             .put("method", "delete")
                             .put("path", "/devices"));
        jsonObject.put("endpoints", endpoints);

        try {
            ApiDefinition.fromJson(jsonObject);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
        }

    }


    @Test
    public void testPutMethod() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("path", "/devices");
        JsonArray endpoints = new JsonArray();
        endpoints.add(new JsonObject()
                              .put("type", "simple-http").put("host", "localhost").put("port", 80)
                              .put("method", "put")
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
        Assert.assertEquals(HttpMethod.PUT, httpEndpoint.method());

        Assert.assertEquals(0, apiDefinition.plugins().size());
    }

    @Test
    public void testGetMethod() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("path", "/devices");
        JsonArray endpoints = new JsonArray();
        endpoints.add(new JsonObject()
                              .put("type", "simple-http").put("host", "localhost").put("port", 80)
                              .put("method", "get")
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
    }

    @Test
    public void testDeleteMethod() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("path", "/devices");
        JsonArray endpoints = new JsonArray();
        endpoints.add(new JsonObject()
                              .put("type", "simple-http").put("host", "localhost").put("port", 80)
                              .put("method", "delete")
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
        Assert.assertEquals(HttpMethod.DELETE, httpEndpoint.method());

        Assert.assertEquals(0, apiDefinition.plugins().size());
    }

    @Test
    public void testPostMethod() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "device.add.1.0.0")
                .put("path", "/devices");
        JsonArray endpoints = new JsonArray();
        endpoints.add(new JsonObject()
                              .put("type", "simple-http").put("host", "localhost").put("port", 80)
                              .put("method", "post")
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
        Assert.assertEquals(HttpMethod.POST, httpEndpoint.method());

        Assert.assertEquals(0, apiDefinition.plugins().size());
    }
}