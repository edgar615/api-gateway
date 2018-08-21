package com.github.edgar615.gateway.core.definition;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/9/8.
 *
 * @author Edgar  Date 2016/9/8
 */
public class SimpleHttpEndPointTest {

    @Test
    public void testBuild() {
        SimpleHttpEndpoint httpEndpoint =
                SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                        80, "localhost");

        Assert.assertEquals("/devices", httpEndpoint.path());
    }

    @Test
    public void testToJson() {
        SimpleHttpEndpoint httpEndpoint =
                SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices",
                                        80, "localhost");

        JsonObject jsonObject = Endpoints.toJson(httpEndpoint);


        Assert.assertEquals("/devices", jsonObject.getString("path"));
        Assert.assertEquals("get_device", jsonObject.getString("name"));
        Assert.assertEquals("simple-http", jsonObject.getString("type"));
        Assert.assertEquals("GET", jsonObject.getString("method"));
        Assert.assertEquals("localhost", jsonObject.getString("host"));
        Assert.assertEquals(80, jsonObject.getInteger("port"), 0);
    }

    @Test
    public void testFromJson() {

        JsonObject jsonObject = new JsonObject()
                .put("type", "simple-http").put("host", "localhost").put("port", 80)
                .put("name", "device.delete.1.2.0")
                .put("host", "localhost")
                .put("port", 80)
                .put("method", "delete")
                .put("path", "/devices");

        SimpleHttpEndpoint endpoint = (SimpleHttpEndpoint) Endpoints.fromJson(jsonObject);


        Assert.assertEquals("/devices", endpoint.path());
        Assert.assertEquals("device.delete.1.2.0", endpoint.name());
        Assert.assertEquals("localhost", endpoint.host());
        Assert.assertEquals(HttpMethod.DELETE, endpoint.method());
    }

}
