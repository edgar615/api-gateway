package com.github.edgar615.gateway.core.rpc;

import com.github.edgar615.gateway.core.rpc.http.SimpleHttpRequest;
import com.github.edgar615.util.base.Randoms;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by Edgar on 2017/1/4.
 *
 * @author Edgar  Date 2017/1/4
 */
public class HttpRpcRequestTest {

    @Test
    public void testCopy() {
        SimpleHttpRequest request =
                SimpleHttpRequest.create(UUID.randomUUID().toString(), UUID.randomUUID
                        ().toString());
        request.setPath(UUID.randomUUID().toString());
        request.setHttpMethod(HttpMethod.POST);
        request.setTimeout(Integer.parseInt(Randoms.randomNumber(5)));
        request.setPort(Integer.parseInt(Randoms.randomNumber(5)));
        request.setHost(UUID.randomUUID().toString());
        request.setBody(new JsonObject().put("userId", UUID.randomUUID().toString()));
        request.addParam("param0", UUID.randomUUID().toString());
        request.addHeader("header0", UUID.randomUUID().toString());

        SimpleHttpRequest copyReq = (SimpleHttpRequest) request.copy();
        Assert.assertEquals(request.path(), copyReq.path());
        Assert.assertEquals(request.method(), copyReq.method());
        Assert.assertEquals(request.timeout(), copyReq.timeout());
        Assert.assertEquals(request.port(), copyReq.port());
        Assert.assertEquals(request.host(), copyReq.host());
        Assert.assertEquals(request.id(), copyReq.id());
        Assert.assertEquals(request.name(), copyReq.name());
        Assert.assertEquals("simple-http", copyReq.type());
        Assert.assertEquals(request.body().getString("userId"), copyReq.body().getString("userId"));
        Assert.assertEquals(request.params().get("param0"), copyReq.params().get("param0"));
        Assert.assertEquals(request.headers().get("header0"), copyReq.headers().get("header0"));

        copyReq.clearHeaders();
        Assert.assertEquals(0, copyReq.headers().size());
        copyReq.clearParams();
        Assert.assertEquals(0, copyReq.params().size());
    }
}