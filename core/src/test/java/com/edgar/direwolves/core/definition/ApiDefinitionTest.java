package com.edgar.direwolves.core.definition;

import com.google.common.collect.Lists;
import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

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

    ApiDefinition apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/", "default", Lists.newArrayList(httpEndpoint));
    Assert.assertEquals("/devices", apiDefinition.path());
    Assert.assertEquals("default", apiDefinition.scope());
  }

  @Test
  public void testEndpointsShouldNotEmpty() {
    try {
      ApiDefinition.create("get_device", HttpMethod.GET, "device/", "default", Lists.newArrayList());
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
    ApiDefinition apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "device/", "default", Lists.newArrayList(httpEndpoint));

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


//  @Test
//  public void testFilter() {
//    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
//        "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//        .setMethod(HttpMethod.GET)
//        .setPath("devices/")
//        .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//
//    apiDefinition.addFilter("jwt");
//    apiDefinition.addFilter("app_key");
//    apiDefinition.addFilter("app_key");
//    Assert.assertEquals(2, apiDefinition.filters().size());
//
//    apiDefinition.removeFilter("app_key");
//
//    Assert.assertEquals(1, apiDefinition.filters().size());
//
//  }


  @Test
  public void testMatcher() {
    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
        "device");
    ApiDefinition apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/", "default", Lists.newArrayList(httpEndpoint));

    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/123"));


    httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices",
        "device");
    apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/([\\d+]+)", "default", Lists.newArrayList(httpEndpoint));

    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/123"));

    httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices",
        "device");
    apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/([\\w+]+)", "default", Lists.newArrayList(httpEndpoint));

    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/123"));
  }
}
