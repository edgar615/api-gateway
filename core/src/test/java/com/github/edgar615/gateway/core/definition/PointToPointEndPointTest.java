package com.github.edgar615.gateway.core.definition;

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
        EventbusEndpoint endpoint =
                EventbusEndpoint.pointToPoint("get_device", "service.device.get", null, null);

        JsonObject jsonObject = Endpoints.toJson(endpoint);

        Assert.assertEquals("eventbus", jsonObject.getString("type"));
        Assert.assertEquals("get_device", jsonObject.getString("name"));
        Assert.assertEquals("point-point", jsonObject.getString("policy"));
        Assert.assertEquals("service.device.get", jsonObject.getString("address"));
        Assert.assertFalse(jsonObject.containsKey("action"));

        endpoint =
                EventbusEndpoint.pointToPoint("get_device", "service.device.get", "get", null);

        jsonObject = Endpoints.toJson(endpoint);

        Assert.assertEquals("eventbus", jsonObject.getString("type"));
        Assert.assertEquals("get_device", jsonObject.getString("name"));
        Assert.assertEquals("point-point", jsonObject.getString("policy"));
        Assert.assertEquals("service.device.get", jsonObject.getString("address"));
        Assert.assertEquals("get", jsonObject.getString("action"));
    }

    @Test
    public void testFromJson() {

        JsonObject jsonObject = new JsonObject()
                .put("type", "eventbus")
                .put("name", "device.delete.1.2.0")
                .put("policy", "point-point")
                .put("address", "service.device.delete");

        EventbusEndpoint endpoint = (EventbusEndpoint) Endpoints.fromJson(jsonObject);

        Assert.assertEquals("service.device.delete", endpoint.address());
        Assert.assertEquals("device.delete.1.2.0", endpoint.name());
        Assert.assertEquals("point-point", endpoint.policy());
        Assert.assertNull(endpoint.action());

        jsonObject = new JsonObject()
                .put("type", "eventbus")
                .put("name", "device.delete.1.2.0")
                .put("policy", "point-point")
                .put("address", "service.device.delete")
                .put("action", "get");

        endpoint = (EventbusEndpoint) Endpoints.fromJson(jsonObject);

        Assert.assertEquals("service.device.delete", endpoint.address());
        Assert.assertEquals("device.delete.1.2.0", endpoint.name());
        Assert.assertEquals("point-point", endpoint.policy());
        Assert.assertEquals("get", endpoint.action());
    }

}
