package com.github.edgar615.gateway.core.definition;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.vertx.core.json.JsonArray;
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
        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put("action", "get");

        EventbusEndpoint endpoint =
                EventbusEndpoint.reqResp("get_device", "service.device.get", null, headers);

        JsonObject jsonObject = Endpoints.toJson(endpoint);

        System.out.println(jsonObject);
        Assert.assertEquals("eventbus", jsonObject.getString("type"));
        Assert.assertEquals("get_device", jsonObject.getString("name"));
        Assert.assertEquals("req-resp", jsonObject.getString("policy"));
        Assert.assertEquals("service.device.get", jsonObject.getString("address"));
        Assert.assertEquals("get",
                            jsonObject.getJsonObject("header").getJsonArray("action").getString(0));
    }

    @Test
    public void testFromJson() {

        JsonObject jsonObject = new JsonObject()
                .put("type", "eventbus")
                .put("name", "device.delete.1.2.0")
                .put("policy", "req-resp")
                .put("address", "service.device.delete")
                .put("header", new JsonObject()
                        .put("action", "get")
                        .put("arr", new JsonArray().add(1).add(2)));

        EventbusEndpoint endpoint = (EventbusEndpoint) Endpoints.fromJson(jsonObject);

        System.out.println(endpoint);
        Assert.assertEquals("service.device.delete", endpoint.address());
        Assert.assertEquals("device.delete.1.2.0", endpoint.name());
        Assert.assertEquals("req-resp", endpoint.policy());
        Assert.assertEquals(3, endpoint.headers().size());
    }

}
