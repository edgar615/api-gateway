package com.edgar.direwolves.definition;

import com.google.common.collect.Lists;
import io.vertx.core.http.HttpMethod;
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
//        Endpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "/devices", "device", null, null, true);
        HttpEndpoint httpEndpoint = HttpEndpoint.builder().setName("get_device")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setService("device")
                .setArray(true).build();

        Assert.assertEquals("/devices", httpEndpoint.getPath());
        Assert.assertNull(httpEndpoint.getBodyArgs());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildGetShouldNoBody() {
        HttpEndpoint httpEndpoint = HttpEndpoint.builder().setName("get_device")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setService("device")
                .setBodyArgs(Lists.newArrayList(new Parameter("username", null)))
                .setArray(true).build();

        Assert.fail();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUrlArgsShouldImmutable() {
        HttpEndpoint httpEndpoint = HttpEndpoint.builder().setName("get_device")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setService("device")
                .setUrlArgs(Lists.newArrayList(new Parameter("username", null)))
                .setArray(true).build();

        httpEndpoint.getUrlArgs().add(new Parameter("password", null));
        Assert.fail();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBodyArgsShouldImmutable() {
        HttpEndpoint httpEndpoint = HttpEndpoint.builder().setName("get_device")
                .setMethod(HttpMethod.POST)
                .setPath("devices/")
                .setService("device")
                .setBodyArgs(Lists.newArrayList(new Parameter("username", null)))
                .setArray(true).build();

        httpEndpoint.getBodyArgs().add(new Parameter("password", null));
        Assert.fail();
    }
}
