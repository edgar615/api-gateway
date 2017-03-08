package com.edgar.direwolves.core.definition;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/9/8.
 *
 * @author Edgar  Date 2016/9/8
 */
public class ReqRespEndPointTest {

  @Test
  public void testToJson() {
    ReqRespEndpoint endpoint =
            Endpoint.reqResp("get_device", "service.device.get", "get");

    JsonObject jsonObject = Endpoints.toJson(endpoint);

    Assert.assertEquals("req-resp", jsonObject.getString("type"));
    Assert.assertEquals("get_device", jsonObject.getString("name"));
    Assert.assertEquals("service.device.get", jsonObject.getString("address"));
    Assert.assertEquals("get", jsonObject.getString("action"));
  }

  @Test
  public void testFromJson() {

    JsonObject jsonObject = new JsonObject()
            .put("type", "req-resp")
            .put("name", "device.delete.1.2.0")
            .put("address", "service.device.delete");

    ReqRespEndpoint endpoint = (ReqRespEndpoint) Endpoints.fromJson(jsonObject);

    Assert.assertEquals("service.device.delete", endpoint.address());
    Assert.assertEquals("device.delete.1.2.0", endpoint.name());
    Assert.assertNull(endpoint.action());
  }

}
