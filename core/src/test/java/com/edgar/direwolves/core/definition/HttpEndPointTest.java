package com.edgar.direwolves.core.definition;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/9/8.
 *
 * @author Edgar  Date 2016/9/8
 */
public class HttpEndPointTest {

  @Test
  public void testBuild() {
    HttpEndpoint httpEndpoint =
            HttpEndpoint.http("get_device", HttpMethod.GET, "/devices", "device");

    Assert.assertEquals("/devices", httpEndpoint.path());
  }

  @Test
  public void testToJson() {
    HttpEndpoint httpEndpoint =
            HttpEndpoint.http("get_device", HttpMethod.GET, "/devices", "device");

    JsonObject jsonObject = Endpoints.toJson(httpEndpoint);


    Assert.assertEquals("/devices", jsonObject.getString("path"));
    Assert.assertEquals("get_device", jsonObject.getString("name"));
    Assert.assertEquals("http", jsonObject.getString("type"));
    Assert.assertEquals("device", jsonObject.getString("service"));
    Assert.assertEquals("GET", jsonObject.getString("method"));
  }

  @Test
  public void testFromJson() {

    JsonObject jsonObject = new JsonObject()
            .put("type", "http")
            .put("name", "device.delete.1.2.0")
            .put("service", "device")
            .put("method", "delete")
            .put("path", "/devices");

    HttpEndpoint endpoint = (HttpEndpoint) Endpoints.fromJson(jsonObject);


    Assert.assertEquals("/devices", endpoint.path());
    Assert.assertEquals("device.delete.1.2.0", endpoint.name());
    Assert.assertEquals("device", endpoint.service());
    Assert.assertEquals(HttpMethod.DELETE, endpoint.method());
  }

}
