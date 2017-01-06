package com.edgar.direwolves.verticle;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import io.vertx.core.http.HttpMethod;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by Edgar on 2016/4/11.
 *
 * @author Edgar  Date 2016/4/11
 */
public class ApiDefinitionRegistryTest {

  ApiDefinitionRegistry registry;

  @Before
  public void setUp() {
    registry = ApiDefinitionRegistry.create();
  }

  @After
  public void clear() {
    registry.remove(null);
  }

  @Test
  public void testRegister() {
    HttpEndpoint httpEndpoint = Endpoint
            .createHttp("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    registry.add(apiDefinition);
    Assert.assertEquals(1, registry.getDefinitions().size());

    apiDefinition = ApiDefinition
            .create("get_device2", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    registry.add(apiDefinition);
    Assert.assertEquals(2, registry.getDefinitions().size());
  }

  @Test
  public void testUniqueName() {
    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    registry.add(apiDefinition);
    registry.add(apiDefinition);
    Assert.assertEquals(1, registry.getDefinitions().size());
  }

  @Test
  public void testFilterByName() {
    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    registry.add(apiDefinition);
    Assert.assertEquals(1, registry.getDefinitions().size());

    apiDefinition = ApiDefinition
            .create("get_device2", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    registry.add(apiDefinition);
    Assert.assertEquals(2, registry.getDefinitions().size());

    List<ApiDefinition> definitions = registry.filter("get_device");
    Assert.assertNotNull(apiDefinition);
    Assert.assertEquals(1, definitions.size());
    Assert.assertEquals("get_device", definitions.get(0).name());

    definitions = registry.filter("get_device3");
    Assert.assertNotNull(apiDefinition);
    Assert.assertEquals(0, definitions.size());

    definitions = registry.filter("get*");
    Assert.assertNotNull(apiDefinition);
    Assert.assertEquals(2, definitions.size());

    definitions = registry.filter("*device*");
    Assert.assertNotNull(apiDefinition);
    Assert.assertEquals(1, definitions.size());

    definitions = registry.filter("**");
    Assert.assertNotNull(apiDefinition);
    Assert.assertEquals(2, definitions.size());

    definitions = registry.filter("*");
    Assert.assertNotNull(apiDefinition);
    Assert.assertEquals(2, definitions.size());

    definitions = registry.filter("***");
    Assert.assertNotNull(apiDefinition);
    Assert.assertEquals(0, definitions.size());
  }

}
