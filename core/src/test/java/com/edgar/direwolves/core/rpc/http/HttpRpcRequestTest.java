package com.edgar.direwolves.core.rpc.http;

import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.util.base.Randoms;
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
    HttpRpcRequest request = HttpRpcRequest.create(UUID.randomUUID().toString(), UUID.randomUUID
            ().toString());
    request.setPath(UUID.randomUUID().toString());
    request.setHttpMethod(HttpMethod.POST);
    request.setTimeout(Integer.parseInt(Randoms.randomNumber(5)));
    request.setPort(Integer.parseInt(Randoms.randomNumber(5)));
    request.setHost(UUID.randomUUID().toString());
    request.setBody(new JsonObject().put("userId", UUID.randomUUID().toString()));
    request.addParam("param0", UUID.randomUUID().toString());
    request.addHeader("header0", UUID.randomUUID().toString());

    HttpRpcRequest copyReq = (HttpRpcRequest) request.copy();
    Assert.assertEquals(request.getPath(), copyReq.getPath());
    Assert.assertEquals(request.getHttpMethod(), copyReq.getHttpMethod());
    Assert.assertEquals(request.getTimeout(), copyReq.getTimeout());
    Assert.assertEquals(request.getPort(), copyReq.getPort());
    Assert.assertEquals(request.getHost(), copyReq.getHost());
    Assert.assertEquals(request.getId(), copyReq.getId());
    Assert.assertEquals(request.getName(), copyReq.getName());
    Assert.assertEquals(request.getBody().getString("userId"), copyReq.getBody().getString("userId"));
    Assert.assertEquals(request.getParams().get("param0"), copyReq.getParams().get("param0"));
    Assert.assertEquals(request.getHeaders().get("header0"), copyReq.getHeaders().get("header0"));
  }
}