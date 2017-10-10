package com.github.edgar615.direwolves.core.definition;

import com.google.common.collect.Lists;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
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

    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");

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
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");
    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");
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
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");
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
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");
    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    MockPlugin plugin = new MockPlugin();
    apiDefinition.addPlugin(plugin);

    Assert.assertEquals(1, apiDefinition.plugins().size());
    apiDefinition.removePlugin(MockPlugin.class.getSimpleName());

    Assert.assertNull(apiDefinition.plugin(MockPlugin.class.getSimpleName()));

  }

  @Test
  public void testMatchName() {

    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    Assert.assertEquals("/devices", apiDefinition.path());

    Assert.assertTrue(apiDefinition.match(new JsonObject().put("name", "get_device")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("name", "get*")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("name", "gEt*")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("name", "*e")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("name", "*")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("name", "***")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("name", "get_device2")));
  }

  @Test
  public void testMatchMethod() {

    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    Assert.assertEquals("/devices", apiDefinition.path());

    Assert.assertTrue(apiDefinition.match(new JsonObject().put("method", "get")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("method", "get*")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("method", "*t")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("method", "*")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("method", "put")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("method", "dfaere")));
  }

  @Test
  public void testMatchPath() {

    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    Assert.assertEquals("/devices", apiDefinition.path());

    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path", "/devices")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path", "/devices/")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices/abc")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices/123")));

    httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");
    apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/([\\d+]+)",
                                         Lists.newArrayList(httpEndpoint));

    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices/")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices/abc")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path", "/devices/123")));

    httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");
    apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/([\\w+]+)",
                                         Lists.newArrayList(httpEndpoint));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices/")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path", "/devices/abc")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path", "/devices/123")));
  }

  @Test
  public void testMatchAll() {


    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");
    ApiDefinition apiDefinition =
            ApiDefinition.create("get_device", HttpMethod.GET, "devices/([\\d+]+)",
                                 Lists.newArrayList(httpEndpoint));

    Assert.assertTrue(apiDefinition.match(new JsonObject()));

    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices")
                                                   .put("name", "*")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices/")
                                                   .put("method", "*")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path", "/devices/123")
                                                  .put("name", "*").put("method", "*")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path", "/devices/123")
                                                  .put("name", "get*").put("method", "GET")));

    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices/123")
                                                  .put("name", "query*").put("method", "*")));
  }

  @Test
  public void testMatchUndefined() {

    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    Assert.assertEquals("/devices", apiDefinition.path());

    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path2", "/devices")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path2", "/devices/")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path2", "/devices/abc")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path2", "/devices/123")));

  }
}
