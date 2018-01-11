package com.github.edgar615.direwolves.core.definition;

import com.google.common.collect.Lists;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/9/8.
 *
 * @author Edgar  Date 2016/9/8
 */
public class AntApiDefinitionTest {

  @Test
  public void testMatchPath() {

    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");

    ApiDefinition apiDefinition = ApiDefinition
            .createAnt("get_device", HttpMethod.GET, "devices/*", Lists.newArrayList(httpEndpoint));

    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path", "/devices/abc")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path", "/devices/123")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices/abc/123")));

  }

  @Test
  public void testIgnore() {

    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                    80, "localhost");

    AntPathApiDefinitionImpl apiDefinition = (AntPathApiDefinitionImpl) ApiDefinition
            .createAnt("get_device", HttpMethod.GET, "devices/**", Lists.newArrayList
                    (httpEndpoint));
    apiDefinition.addIgnoredPattern("/devices/123");
    apiDefinition.addIgnoredPattern("/devices/abc/**");

    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path", "/devices")));
    Assert.assertTrue(apiDefinition.match(new JsonObject().put("path", "/devices/123/abc")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices/abc")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices/123")));
    Assert.assertFalse(apiDefinition.match(new JsonObject().put("path", "/devices/abc/123")));

  }
}
