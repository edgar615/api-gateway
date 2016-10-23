package com.edgar.direwolves.definition;

import com.edgar.direwolves.plugin.acl.AclRestriction;
import com.edgar.direwolves.plugin.arg.BodyArgPlugin;
import com.edgar.direwolves.plugin.arg.Parameter;
import com.edgar.direwolves.plugin.arg.UrlArgPlugin;
import com.edgar.direwolves.plugin.ip.IpRestriction;
import com.edgar.direwolves.plugin.ratelimit.RateLimitPlugin;
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
  }

  @Test
  public void testUrlArgs() {
    JsonObject jsonObject = new JsonObject()
        .put("name", "device.add.1.0.0")
        .put("method", "post")
        .put("path", "/devices")
        .put("scope", "device:write");
    JsonArray endpoints = new JsonArray();
    endpoints.add(new JsonObject()
        .put("type", "http")
        .put("name", "device.add.1.2.0")
        .put("service", "device")
        .put("method", "undefined")
        .put("path", "/devices/add"));
    jsonObject.put("endpoints", endpoints);

    jsonObject.put("strict_arg", true);
    JsonArray parameters = new JsonArray();
    jsonObject.put("url_arg", parameters);
    JsonObject urlArg1 = new JsonObject()
        .put("name", "macAddress")
        .put("default_value", "FFFFFFFFFFFF")
        .put("rules", new JsonObject().put("required", true)
            .put("regex", "[0-9A-F]{16}"));
    parameters.add(urlArg1);

    JsonObject urlArg2 = new JsonObject()
        .put("name", "type")
        .put("rules", new JsonObject().put("required", true)
            .put("integer", true));
    parameters.add(urlArg2);

    JsonObject urlArg3 = new JsonObject()
        .put("name", "barcode");
    parameters.add(urlArg3);

    ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
    Assert.assertEquals("/devices", apiDefinition.path());
    Assert.assertEquals(HttpMethod.POST, apiDefinition.method());

    Assert.assertEquals(1, apiDefinition.endpoints().size());
    Assert.assertEquals("device:write", apiDefinition.scope());

    UrlArgPlugin plugin = (UrlArgPlugin) apiDefinition.plugin(UrlArgPlugin.NAME);

    Assert.assertEquals(3, plugin.parameters().size());
    Parameter parameter1 = plugin.parameters().get(0);
    Assert.assertEquals(2, parameter1.rules().size());
    Assert.assertEquals("FFFFFFFFFFFF", parameter1.defaultValue());

    Parameter parameter2 = plugin.parameters().get(1);
    Assert.assertEquals(2, parameter2.rules().size());
    Assert.assertNull(parameter2.defaultValue());

    Parameter parameter3 = plugin.parameters().get(2);
    Assert.assertEquals(0, parameter3.rules().size());
  }

  @Test
  public void testBodyArgs() {
    JsonObject jsonObject = new JsonObject()
        .put("name", "device.add.1.0.0")
        .put("method", "post")
        .put("path", "/devices")
        .put("scope", "device:write");
    JsonArray endpoints = new JsonArray();
    endpoints.add(new JsonObject()
        .put("type", "http")
        .put("name", "device.add.1.2.0")
        .put("service", "device")
        .put("method", "undefined")
        .put("path", "/devices/add"));
    jsonObject.put("endpoints", endpoints);

    jsonObject.put("strict_arg", true);
    JsonArray bodyArgs = new JsonArray();
    jsonObject.put("body_arg", bodyArgs);
    JsonObject bodyArg1 = new JsonObject()
        .put("name", "macAddress")
        .put("default_value", "FFFFFFFFFFFF")
        .put("rules", new JsonObject().put("required", true)
            .put("regex", "[0-9A-F]{16}"));
    bodyArgs.add(bodyArg1);

    JsonObject bodyArg2 = new JsonObject()
        .put("name", "type")
        .put("rules", new JsonObject().put("required", true)
            .put("integer", true));
    bodyArgs.add(bodyArg2);

    JsonObject bodyArg3 = new JsonObject()
        .put("name", "barcode");
    bodyArgs.add(bodyArg3);

    ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
    Assert.assertEquals("/devices", apiDefinition.path());
    Assert.assertEquals(HttpMethod.POST, apiDefinition.method());

    Assert.assertEquals(1, apiDefinition.endpoints().size());
    Assert.assertEquals("device:write", apiDefinition.scope());
    BodyArgPlugin plugin = (BodyArgPlugin) apiDefinition.plugin(BodyArgPlugin.NAME);

    Assert.assertEquals(3, plugin.parameters().size());
    Parameter parameter1 = plugin.parameters().get(0);
    Assert.assertEquals(2, parameter1.rules().size());
    Assert.assertEquals("FFFFFFFFFFFF", parameter1.defaultValue());

    Parameter parameter2 = plugin.parameters().get(1);
    Assert.assertEquals(2, parameter2.rules().size());
    Assert.assertNull(parameter2.defaultValue());

    Parameter parameter3 = plugin.parameters().get(2);
    Assert.assertEquals(0, parameter3.rules().size());
  }

  @Test
  public void testIpRestriction() {
//    JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");
    JsonObject jsonObject = new JsonObject()
        .put("name", "device.add.1.0.0")
        .put("path", "/devices")
        .put("whitelist", new JsonArray().add("1").add("2").add("2"));
    JsonArray endpoints = new JsonArray();
    endpoints.add(new JsonObject()
        .put("type", "http")
        .put("name", "device.add.1.2.0")
        .put("service", "device")
        .put("path", "/devices/add"));
    jsonObject.put("endpoints", endpoints);
    JsonArray whitelist = new JsonArray().add("1").add("2");
    JsonArray blacklist = new JsonArray().add("3");
    jsonObject.put("ip_restriction", new JsonObject()
        .put("whitelist", whitelist)
        .put("blacklist", blacklist));

    ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);

    IpRestriction plugin = (IpRestriction) apiDefinition.plugin(IpRestriction.NAME);
    Assert.assertEquals(2, plugin.whitelist().size());
    Assert.assertEquals(1, plugin.blacklist().size());
  }

  @Test
  public void testAclRestriction() {
//    JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");
    JsonObject jsonObject = new JsonObject()
        .put("name", "device.add.1.0.0")
        .put("path", "/devices")
        .put("whitelist", new JsonArray().add("1").add("2").add("2"));
    JsonArray endpoints = new JsonArray();
    endpoints.add(new JsonObject()
        .put("type", "http")
        .put("name", "device.add.1.2.0")
        .put("service", "device")
        .put("path", "/devices/add"));
    jsonObject.put("endpoints", endpoints);
    JsonArray whitelist = new JsonArray().add("1").add("2");
    JsonArray blacklist = new JsonArray().add("3");
    jsonObject.put("acl_restriction", new JsonObject()
        .put("whitelist", whitelist)
        .put("blacklist", blacklist));

    ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);

    AclRestriction plugin = (AclRestriction) apiDefinition.plugin(AclRestriction.NAME);
    Assert.assertEquals(2, plugin.whitelist().size());
    Assert.assertEquals(1, plugin.blacklist().size());
  }

  @Test
  public void testRateLimit() {
//    JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");
    JsonObject jsonObject = new JsonObject()
        .put("name", "device.add.1.0.0")
        .put("path", "/devices");

    JsonArray rateLimits = new JsonArray();
    jsonObject.put("rate_limit", rateLimits);
    rateLimits.add(new JsonObject()
        .put("limit", 1000)
        .put("type", "day")
        .put("limit_by", "token"))
        .add(new JsonObject()
            .put("limit", 100)
            .put("type", "day")
            .put("limit_by", "token"))
        .add(new JsonObject()
            .put("limit", 100)
            .put("type", "second")
            .put("limit_by", "token"));

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

    RateLimitPlugin plugin = (RateLimitPlugin) apiDefinition.plugin(RateLimitPlugin.NAME);
    Assert.assertEquals(2, plugin.rateLimits().size());

    System.out.println(apiDefinition);
    plugin.rateLimits().forEach(ratelimit -> Assert.assertEquals(100, ratelimit.limit()));
  }
}
