package com.edgar.direwolves.core.definition;

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
public class ApiDefinitionDecoderTest {

  @Test
  public void testMinimizeConfig() {
//    JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");
    JsonObject jsonObject = new JsonObject()
        .put("name", "device.add.1.0.0")
        .put("path", "/devices");
    JsonArray endpoints = new JsonArray();
    endpoints.add(new JsonObject()
        .put("type", "http")
        .put("name", "device.add.1.2.0")
        .put("service", "device")
        .put("path", "/devices/add"));
    jsonObject.put("endpoints", endpoints);
    ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
    Assert.assertEquals(HttpMethod.GET, apiDefinition.method());
    Assert.assertEquals("/devices", apiDefinition.path());
    Assert.assertEquals(1, apiDefinition.endpoints().size());
    Assert.assertEquals("default", apiDefinition.scope());
    Endpoint endpoint = apiDefinition.endpoints().get(0);
    Assert.assertTrue(endpoint instanceof HttpEndpoint);
    HttpEndpoint httpEndpoint = (HttpEndpoint) endpoint;
    Assert.assertEquals("/devices/add", httpEndpoint.path());
    Assert.assertEquals("device.add.1.2.0", httpEndpoint.name());
    Assert.assertEquals(HttpMethod.GET, httpEndpoint.method());

    Assert.assertEquals(0, apiDefinition.plugins().size());
  }

  @Test
  public void testTwoEndpoints() {
//    JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");
    JsonObject jsonObject = new JsonObject()
        .put("name", "device.add.1.0.0")
        .put("method", "post")
        .put("path", "/devices");
    JsonArray endpoints = new JsonArray();
    endpoints.add(new JsonObject()
        .put("type", "http")
        .put("name", "device.add.1.2.0")
        .put("service", "device")
        .put("method", "undefined")
        .put("path", "/devices/add"))
        .add(new JsonObject()
            .put("type", "http")
            .put("name", "device.delete.1.2.0")
            .put("service", "device")
            .put("method", "delete")
            .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);

    ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
    Assert.assertEquals("/devices", apiDefinition.path());
    Assert.assertEquals(HttpMethod.POST, apiDefinition.method());

    Assert.assertEquals(2, apiDefinition.endpoints().size());
    Assert.assertEquals("default", apiDefinition.scope());
    Endpoint endpoint = apiDefinition.endpoints().get(0);
    Assert.assertTrue(endpoint instanceof HttpEndpoint);
    HttpEndpoint httpEndpoint = (HttpEndpoint) endpoint;
    Assert.assertEquals("/devices/add", httpEndpoint.path());
    Assert.assertEquals("device.add.1.2.0", httpEndpoint.name());
    Assert.assertEquals(HttpMethod.GET, httpEndpoint.method());

    endpoint = apiDefinition.endpoints().get(1);
    Assert.assertTrue(endpoint instanceof HttpEndpoint);
    httpEndpoint = (HttpEndpoint) endpoint;
    Assert.assertEquals("/devices", httpEndpoint.path());
    Assert.assertEquals("device.delete.1.2.0", httpEndpoint.name());
    Assert.assertEquals(HttpMethod.DELETE, httpEndpoint.method());

    Assert.assertEquals(0, apiDefinition.plugins().size());
  }
}