package com.edgar.direwolves.core.definition;

import com.google.common.collect.Lists;

import com.edgar.util.base.Randoms;
import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

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

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    Assert.assertEquals("/devices", apiDefinition.path());

    System.out.println(apiDefinition);
  }

  @Test
  public void testEndpointsShouldNotEmpty() {
    try {
      ApiDefinition.create("get_device", HttpMethod.GET, "device/", Lists.newArrayList());
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
    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

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
  public void testAddPlugin() {
    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    MockPlugin plugin = new MockPlugin();
    apiDefinition.addPlugin(plugin);
    apiDefinition.addPlugin(null);

    Assert.assertEquals(1, apiDefinition.plugins().size());

    Assert.assertSame(plugin, apiDefinition.plugin(MockPlugin.class.getSimpleName()));

    Assert.assertNull(apiDefinition.plugin(UUID.randomUUID().toString()));

  }

  @Test
  public void testRemovePlugin() {
    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    MockPlugin plugin = new MockPlugin();
    apiDefinition.addPlugin(plugin);

    Assert.assertEquals(1, apiDefinition.plugins().size());
    apiDefinition.removePlugin(MockPlugin.class.getSimpleName());

    Assert.assertNull(apiDefinition.plugin(MockPlugin.class.getSimpleName()));

  }


  @Test
  public void testMatcher() {
    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
                                                    "device");
    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));

    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/123"));


    httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices",
                                       "device");
    apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/([\\d+]+)",
                                         Lists.newArrayList(httpEndpoint));

    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/123"));

    httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices",
                                       "device");
    apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/([\\w+]+)",
                                         Lists.newArrayList(httpEndpoint));

    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/123"));
  }
}
