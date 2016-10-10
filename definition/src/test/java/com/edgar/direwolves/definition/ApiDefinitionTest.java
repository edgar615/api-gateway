package com.edgar.direwolves.definition;

import com.google.common.collect.Lists;

import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2016/9/8.
 *
 * @author Edgar  Date 2016/9/8
 */
public class ApiDefinitionTest {

  @Test
  public void testCreate() {

    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint));
    ApiDefinition apiDefinition = ApiDefinition.create(option);
    Assert.assertEquals("/devices", apiDefinition.path());
    Assert.assertEquals("default", apiDefinition.scope());
  }

  @Test
  public void testEndpointsShouldNotEmpty() {
    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList());
    try {
      ApiDefinition.create(option);
      Assert.fail();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      Assert.assertTrue(e instanceof IllegalArgumentException);
    }
  }

  @Test
  public void testEndpointsShouldImmutable() {
    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint));
    ApiDefinition apiDefinition = ApiDefinition.create(option);

    httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
    try {
      apiDefinition.endpoints().add(httpEndpoint);
      Assert.fail();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      Assert.assertTrue(e instanceof UnsupportedOperationException);
    }
  }

  @Test
  public void testUrlArgsShouldImmutable() {
    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setUrlArgs(Lists.newArrayList(new ParameterImpl("username", null)))
            .setEndpoints(Lists.newArrayList(httpEndpoint));
    ApiDefinition apiDefinition = ApiDefinition.create(option);
    try {
      apiDefinition.urlArgs().add(new ParameterImpl("password", null));
      Assert.fail();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      Assert.assertTrue(e instanceof UnsupportedOperationException);
    }
  }

    @Test
    public void testBodyArgsShouldImmutable() {
      HttpEndpoint httpEndpoint =
              Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
      ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
              .setMethod(HttpMethod.POST)
              .setPath("devices/")
              .setBodyArgs(Lists.newArrayList(new ParameterImpl("username", null)))
              .setEndpoints(Lists.newArrayList(httpEndpoint));
      ApiDefinition apiDefinition = ApiDefinition.create(option);
      try {
        apiDefinition.bodyArgs().add(new ParameterImpl("password", null));
        Assert.fail();
      } catch (Exception e) {
        System.out.println(e.getMessage());
        Assert.assertTrue(e instanceof UnsupportedOperationException);
      }
    }


  @Test
  public void testFilter() {
    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
                                                    "device");
    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint));
    ApiDefinition apiDefinition = ApiDefinition.create(option);

    apiDefinition.addFilter("jwt");
    apiDefinition.addFilter("app_key");
    apiDefinition.addFilter("app_key");
    Assert.assertEquals(2, apiDefinition.filters().size());

    apiDefinition.removeFilter("app_key");

    Assert.assertEquals(1, apiDefinition.filters().size());

  }

  @Test
  public void testBlackList() {
    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
                                                    "device");
    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint));
    ApiDefinition apiDefinition = ApiDefinition.create(option);

    apiDefinition.addBlacklist("192.168.1.100");
    Assert.assertEquals(1, apiDefinition.blacklist().size());
    apiDefinition.addBlacklist("192.168.1.100");
    apiDefinition.addBlacklist("192.168.1.101");

    Assert.assertEquals(2, apiDefinition.blacklist().size());

    apiDefinition.removeBlacklist("192.168.1.101");

    Assert.assertEquals(1, apiDefinition.blacklist().size());

  }

  @Test
  public void testWhiteList() {
    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
                                                    "device");
    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint));
    ApiDefinition apiDefinition = ApiDefinition.create(option);

    apiDefinition.addWhitelist("192.168.1.100");
    Assert.assertEquals(1, apiDefinition.whitelist().size());
    apiDefinition.addWhitelist("192.168.1.100");
    apiDefinition.addWhitelist("192.168.1.101");

    Assert.assertEquals(2, apiDefinition.whitelist().size());

    apiDefinition.removeWhitelist("192.168.1.101");

    Assert.assertEquals(1, apiDefinition.whitelist().size());

  }

  @Test
  public void testUniqueRateLimit() {
    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
                                                    "device");
    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint));
    ApiDefinition apiDefinition = ApiDefinition.create(option);

    apiDefinition.addRateLimit(RateLimit.create("token", "second", 100));
    apiDefinition.addRateLimit(RateLimit.create("token", "day", 100));
    apiDefinition.addRateLimit(RateLimit.create("user", "second", 100));
    Assert.assertEquals(3, apiDefinition.rateLimits().size());

    apiDefinition.addRateLimit(RateLimit.create("token", "second", 1000));
    apiDefinition.addRateLimit(RateLimit.create("token", "day", 1000));
    apiDefinition.addRateLimit(RateLimit.create("user", "second", 1000));
    Assert.assertEquals(3, apiDefinition.rateLimits().size());

    List<RateLimit> filterDefintions = apiDefinition.rateLimits().stream()
            .filter(d -> "token".equalsIgnoreCase(d.limitBy())
                         && "day".equalsIgnoreCase(d.type()))
            .collect(Collectors.toList());
    RateLimit rateLimit = filterDefintions.get(0);
    Assert.assertEquals(1000, rateLimit.limit());
  }

  @Test
  public void testMatcher() {
    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
                                                    "device");
    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint));
    ApiDefinition apiDefinition = ApiDefinition.create(option);

    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/123"));


    httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices",
                                                    "device");
    option = new ApiDefinitionOption().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/([\\d+]+)")
            .setEndpoints(Lists.newArrayList(httpEndpoint));
    apiDefinition = ApiDefinition.create(option);

    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/123"));

    httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices",
                                       "device");
    option = new ApiDefinitionOption().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/([\\w+]+)")
            .setEndpoints(Lists.newArrayList(httpEndpoint));
    apiDefinition = ApiDefinition.create(option);

    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/123"));
  }
}
