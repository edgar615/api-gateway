package com.edgar.direwolves.core.definition;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/9/8.
 *
 * @author Edgar  Date 2016/9/8
 */
public class PointToPointEndPointTest {

  @Test
  public void testToJson() {
    PointToPointEndpoint endpoint =
            Endpoint.pointToPoint("get_device", "service.device.get");

    JsonObject jsonObject = Endpoints.toJson(endpoint);

    Assert.assertEquals("point", jsonObject.getString("type"));
    Assert.assertEquals("get_device", jsonObject.getString("name"));
    Assert.assertEquals("service.device.get", jsonObject.getString("address"));
  }

  @Test
  public void testFromJson() {

    JsonObject jsonObject = new JsonObject()
            .put("type", "point")
            .put("name", "device.delete.1.2.0")
            .put("address", "service.device.delete");

    PointToPointEndpoint endpoint = (PointToPointEndpoint) Endpoints.fromJson(jsonObject);

    Assert.assertEquals("service.device.delete", endpoint.address());
    Assert.assertEquals("device.delete.1.2.0", endpoint.name());
  }

}
