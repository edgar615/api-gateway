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
  public void testBuild() {

    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device", null, null);

    ApiDefinition apiDefinition = ApiDefinition.builder().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint))
            .build();
    Assert.assertEquals("/devices", apiDefinition.path());
    Assert.assertEquals("default", apiDefinition.scope());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEndpointsShouldNotEmpty() {

    ApiDefinition apiDefinition = ApiDefinition.builder().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList())
            .build();
    Assert.fail();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testEndpointsShouldImmutable() {
    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device", null, null);
    ApiDefinition apiDefinition = ApiDefinition.builder().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint))
            .build();

    httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device", null, null);
    apiDefinition.endpoints().add(httpEndpoint);

    Assert.fail();
  }

//    @Test(expected = UnsupportedOperationException.class)
//    public void testUrlArgsShouldImmutable() {
//        HttpEndpoint httpEndpoint = HttpEndpoint.builder().setName("get_device")
//                .setMethod(HttpMethod.GET)
//                .setPath("devices/")
//                .setService("device")
//                .setArray(true).build();
//        ApiDefinition apiDefinition = ApiDefinition.builder().setName("get_device")
//                .setMethod(HttpMethod.GET)
//                .setPath("devices/")
//                .setEndpoints(Lists.newArrayList(httpEndpoint))
//                .setUrlArgs(Lists.newArrayList(new Parameter("username", null)))
//                .build();
//
//        apiDefinition.urlArgs().add(new Parameter("password", null));
//        Assert.fail();
//    }
//
//    @Test(expected = UnsupportedOperationException.class)
//    public void testBodyArgsShouldImmutable() {
//        HttpEndpoint httpEndpoint = HttpEndpoint.builder().setName("get_device")
//                .setMethod(HttpMethod.GET)
//                .setPath("devices/")
//                .setService("device")
//                .setArray(true).build();
//        ApiDefinition apiDefinition = ApiDefinition.builder().setName("get_device")
//                .setMethod(HttpMethod.POST)
//                .setPath("devices/")
//                .setEndpoints(Lists.newArrayList(httpEndpoint))
//                .setBodyArgs(Lists.newArrayList(new Parameter("username", null)))
//                .build();
//
//        apiDefinition.bodyArgs().add(new Parameter("password", null));
//        Assert.fail();
//    }


  @Test
  public void testFilter() {
    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
                                                    "device", null, null);
    ApiDefinition apiDefinition = ApiDefinition.builder().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint))
            .build();

    apiDefinition.addFilter("jwt");
    apiDefinition.addFilter("app_key");
    Assert.assertEquals(2, apiDefinition.filters().size());

    apiDefinition.removeFilter("app_key");

    Assert.assertEquals(1, apiDefinition.filters().size());

  }

  @Test
  public void testBlackList() {
    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
                                                    "device", null, null);
    ApiDefinition apiDefinition = ApiDefinition.builder().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint))
            .build();

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
                                                    "device", null, null);
    ApiDefinition apiDefinition = ApiDefinition.builder().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint))
            .build();

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
                                                    "device", null, null);
    ApiDefinition apiDefinition = ApiDefinition.builder().setName("get_device")
            .setMethod(HttpMethod.GET)
            .setPath("devices/")
            .setEndpoints(Lists.newArrayList(httpEndpoint))
            .build();

    apiDefinition.add(RateLimit.create("token", "second", 100));
    apiDefinition.add(RateLimit.create("token", "day", 100));
    apiDefinition.add(RateLimit.create("user", "second", 100));
    Assert.assertEquals(3, apiDefinition.rateLimits().size());

    apiDefinition.add(RateLimit.create("token", "second", 1000));
    apiDefinition.add(RateLimit.create("token", "day", 1000));
    apiDefinition.add(RateLimit.create("user", "second", 1000));
    Assert.assertEquals(3, apiDefinition.rateLimits().size());

    List<RateLimit> filterDefintions = apiDefinition.rateLimits().stream()
            .filter(d -> "token".equalsIgnoreCase(d.limitBy())
                         && "day".equalsIgnoreCase(d.type()))
            .collect(Collectors.toList());
    RateLimit rateLimit = filterDefintions.get(0);
    Assert.assertEquals(1000, rateLimit.limit());
  }
}
