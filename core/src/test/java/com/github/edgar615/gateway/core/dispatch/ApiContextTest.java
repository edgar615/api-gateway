//package com.github.edgar615.gateway.core.dispatch;
//
//import HttpRpcRequest;
//import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Multimap;
//
//import ApiDefinition;
//import Endpoint;
//import HttpEndpoint;
//import RpcResponse;
//import com.github.edgar615.util.base.Randoms;
//import io.vertx.core.http.HttpMethod;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.util.List;
//import java.util.UUID;
//
///**
// * Created by Edgar on 2016/9/12.
// *
// * @author Edgar  Date 2016/9/12
// */
//public class ApiContextTest {
//
//  @Test
//  public void testJsonCopy() {
//    JsonObject jsonObject = new JsonObject("{\"accountType\":1,\"username\":\"57516243616\",\n"
//        + "            \"password\":\"111111\"}");
//    System.out.println(jsonObject.encode());
//    System.out.println(jsonObject.copy());
//    JsonObject copyJson = new JsonObject();
//    for (String key : jsonObject.fieldNames()) {
//      copyJson.put(key, jsonObject.getValue(key));
//    }
//    System.out.println(copyJson.encode());
//
//  }
//
//  @Test
//  public void testCopyWithoutBody() {
//    Multimap<String, String> headers = ArrayListMultimap.create();
//    headers.put("h1", "h1.1");
//    headers.put("h1", "h1.2");
//    Multimap<String, String> params = ArrayListMultimap.create();
//    params.put("p1", "p1.1");
//    params.put("p1", "p1.2");
//    params.put("p2", "p2");
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", headers,
//            params, null);
//
//    ApiContext copyContext = apiContext.copy();
//    Assert.assertEquals(apiContext.id(), copyContext.id());
//    Assert.assertEquals(HttpMethod.GET, copyContext.method());
//    Assert.assertEquals("/devices", copyContext.path());
//    Assert.assertEquals("h1.1", copyContext.headers().get("h1").iterator().next());
//    Assert.assertEquals("p1.1", copyContext.params().get("p1").iterator().next());
//    Assert.assertEquals("p2", copyContext.params().get("p2").iterator().next());
//    Assert.assertNull(copyContext.body());
//    System.out.println(copyContext);
//  }
//
//  @Test
//  public void testCopyWithBody() {
//    Multimap<String, String> headers = ArrayListMultimap.create();
//    headers.put("h1", "h1.1");
//    headers.put("h1", "h1.2");
//    Multimap<String, String> params = ArrayListMultimap.create();
//    params.put("p1", "p1.1");
//    params.put("p1", "p1.2");
//    params.put("p2", "p2");
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", headers,
//            params, new JsonObject("{\"accountType\":1,\"username\":\"57516243616\",\n"
//                + "            \"password\":\"111111\"}"));
//
//    ApiContext copyContext = apiContext.copy();
//    Assert.assertEquals(apiContext.id(), copyContext.id());
//    Assert.assertEquals(HttpMethod.GET, copyContext.method());
//    Assert.assertEquals("/devices", copyContext.path());
//    Assert.assertEquals("h1.1", copyContext.headers().get("h1").iterator().next());
//    Assert.assertEquals("p1.1", copyContext.params().get("p1").iterator().next());
//    Assert.assertEquals("p2", copyContext.params().get("p2").iterator().next());
//    Assert.assertNotNull(copyContext.body());
//    Assert.assertEquals("111111", copyContext.body().getString("password"));
//    Assert.assertEquals("accountType", copyContext.body().fieldNames().iterator().next());
//
//    System.out.println(copyContext);
//
//  }
//
//  @Test
//  public void testCopyRequest() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null, null, null);
//
//    HttpRpcRequest rpcRequest =
//        HttpRpcRequest.create(UUID.randomUUID().toString(), UUID.randomUUID().toString());
//    rpcRequest.setBody(new JsonObject().put("foo", "bar"));
//    rpcRequest.setHost(UUID.randomUUID().toString());
//    rpcRequest.setPort(Integer.parseInt(Randoms.randomNumber(5)));
//    rpcRequest.setHttpMethod(HttpMethod.POST);
//    rpcRequest.setPath(UUID.randomUUID().toString());
//    rpcRequest.setTimeout(Integer.parseInt(Randoms.randomNumber(5)));
//    rpcRequest.addHeader("h1", "h1.1");
//    rpcRequest.addHeader("h1", "h1.1");
//    rpcRequest.addHeader("h2", "h2");
//    rpcRequest.addParam("q1", "q1.1");
//    rpcRequest.addParam("q1", "q1.1");
//    rpcRequest.addParam("q2", "q2");
//    apiContext.addRequest(rpcRequest);
//
//    ApiContext copyContext = apiContext.copy();
//    Assert.assertEquals(apiContext.id(), copyContext.id());
//    Assert.assertEquals(HttpMethod.GET, copyContext.method());
//    Assert.assertEquals("/devices", copyContext.path());
//    Assert.assertEquals(1, copyContext.requests().size());
//    HttpRpcRequest copyReq = (HttpRpcRequest) copyContext.requests().get(0);
//    Assert.assertEquals(rpcRequest.id(), copyReq.id());
//    Assert.assertEquals(rpcRequest.host(), copyReq.host());
//    Assert.assertEquals(rpcRequest.port(), copyReq.port());
//    Assert.assertEquals(rpcRequest.timeout(), copyReq.timeout());
//    Assert.assertEquals(rpcRequest.method(), copyReq.method());
//    Assert.assertEquals(rpcRequest.path(), copyReq.path());
//
//    Assert.assertEquals("h1.1", copyReq.headers().get("h1").iterator().next());
//    Assert.assertEquals("h2", copyReq.headers().get("h2").iterator().next());
//    Assert.assertEquals("q1.1", copyReq.params().get("q1").iterator().next());
//    Assert.assertEquals("q2", copyReq.params().get("q2").iterator().next());
//    Assert.assertNotNull(copyReq.body());
//    Assert.assertEquals("bar", copyReq.body().getString("foo"));
//    System.out.println(copyContext);
//  }
//
//  @Test
//  public void testCopyResponse() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null, null, null);
//
//    RpcResponse rpcResponse = RpcResponse.createJsonObject(UUID.randomUUID().toString(),
//        Integer.parseInt(
//            Randoms.randomNumber(5)),
//        new JsonObject().put("foo", "bar"),
//        Long.parseLong(
//            Randoms.randomNumber(10)));
//
//    apiContext.addResponse(rpcResponse);
//
//    ApiContext copyContext = apiContext.copy();
//    Assert.assertEquals(apiContext.id(), copyContext.id());
//    Assert.assertEquals(HttpMethod.GET, copyContext.method());
//    Assert.assertEquals("/devices", copyContext.path());
//    Assert.assertEquals(1, copyContext.responses().size());
//    RpcResponse copyResp = copyContext.responses().get(0);
//    Assert.assertEquals(rpcResponse.id(), copyResp.id());
//    Assert.assertEquals(rpcResponse.elapsedTime(), copyResp.elapsedTime());
//    Assert.assertEquals(rpcResponse.statusCode(), copyResp.statusCode());
//    Assert.assertEquals(rpcResponse.isArray(), copyResp.isArray());
//    Assert.assertEquals("bar", copyResp.responseObject().getString("foo"));
//
//    System.out.println(copyContext);
//  }
//
//  @Test
//  public void testCopyJsonArrayResponse() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null, null, null);
//
//    RpcResponse rpcResponse = RpcResponse.createJsonArray(UUID.randomUUID().toString(),
//        Integer.parseInt(Randoms.randomNumber(5)),
//        new JsonArray().add(1).add("2"),
//        Long.parseLong(Randoms.randomNumber(10)));
//
//    apiContext.addResponse(rpcResponse);
//
//    ApiContext copyContext = apiContext.copy();
//    Assert.assertEquals(apiContext.id(), copyContext.id());
//    Assert.assertEquals(HttpMethod.GET, copyContext.method());
//    Assert.assertEquals("/devices", copyContext.path());
//    Assert.assertEquals(1, copyContext.responses().size());
//    RpcResponse copyResp = copyContext.responses().get(0);
//    Assert.assertEquals(rpcResponse.id(), copyResp.id());
//    Assert.assertEquals(rpcResponse.elapsedTime(), copyResp.elapsedTime());
//    Assert.assertEquals(rpcResponse.statusCode(), copyResp.statusCode());
//    Assert.assertEquals(rpcResponse.isArray(), copyResp.isArray());
//    Assert.assertEquals(1, rpcResponse.responseArray().getInteger(0), 0);
//    Assert.assertEquals("2", rpcResponse.responseArray().getString(1));
//
//    System.out.println(copyContext);
//  }
//
//  @Test
//  public void testCopyVariable() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null, null, null);
//
//    apiContext.addVariable("foo", "bar");
//    apiContext.addVariable("intVal", 1);
//
//    ApiContext copyContext = apiContext.copy();
//    Assert.assertEquals(apiContext.id(), copyContext.id());
//    Assert.assertEquals(HttpMethod.GET, copyContext.method());
//    Assert.assertEquals("/devices", copyContext.path());
//    Assert.assertEquals("bar", apiContext.variables().get("foo"));
//    Assert.assertEquals(1, apiContext.variables().get("intVal"));
//
//    System.out.println(copyContext);
//  }
//
//  @Test
//  public void testCopyResult() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null, null, null);
//
//
//    Multimap<String, String> header = ArrayListMultimap.create();
//    header.put("h1", "h1.1");
//    header.put("h1", "h1.2");
//    header.put("h2", "h2");
//
//    Result result = Result.createJsonObject(Integer.parseInt(Randoms.randomNumber(5)),
//        new JsonObject().put("foo", "bar"), header);
//    apiContext.setResult(result);
//
//    ApiContext copyContext = apiContext.copy();
//    Assert.assertEquals(apiContext.id(), copyContext.id());
//    Assert.assertEquals(HttpMethod.GET, copyContext.method());
//    Assert.assertEquals("/devices", copyContext.path());
//    Result copyResult = copyContext.result();
//    Assert.assertEquals(result.statusCode(), copyResult.statusCode());
//    Assert.assertEquals(result.isArray(), result.isArray());
//    Assert.assertEquals("bar", result.responseObject().getString("foo"));
//    Assert.assertEquals("h1.1", result.header().get("h1").iterator().next());
//    Assert.assertEquals("h2", result.header().get("h2").iterator().next());
//
//    System.out.println(copyContext);
//  }
//
//  @Test
//  public void testCopyJsonArrayResult() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null, null, null);
//
//
//    Multimap<String, String> header = ArrayListMultimap.create();
//    header.put("h1", "h1.1");
//    header.put("h1", "h1.2");
//    header.put("h2", "h2");
//
//    Result result = Result.createJsonArray(Integer.parseInt(Randoms.randomNumber(5)),
//        new JsonArray().add(1).add("2"), header);
//    apiContext.setResult(result);
//
//    ApiContext copyContext = apiContext.copy();
//    Assert.assertEquals(apiContext.id(), copyContext.id());
//    Assert.assertEquals(HttpMethod.GET, copyContext.method());
//    Assert.assertEquals("/devices", copyContext.path());
//    Result copyResult = copyContext.result();
//    Assert.assertEquals(result.statusCode(), copyResult.statusCode());
//    Assert.assertEquals(result.isArray(), result.isArray());
//    Assert.assertEquals(1, result.responseArray().getInteger(0), 0);
//    Assert.assertEquals("2", result.responseArray().getString(1));
//    Assert.assertEquals("h1.1", result.header().get("h1").iterator().next());
//    Assert.assertEquals("h2", result.header().get("h2").iterator().next());
//
//    System.out.println(copyContext);
//  }
//
//  @Test
//  public void testCopyPrincipal() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null, null, null);
//
//    Multimap<String, String> header = ArrayListMultimap.create();
//    header.put("h1", "h1.1");
//    header.put("h1", "h1.2");
//    header.put("h2", "h2");
//
//    apiContext.setPrincipal(new JsonObject().put("userId", "edgar"));
//
//
//    ApiContext copyContext = apiContext.copy();
//    Assert.assertEquals(apiContext.id(), copyContext.id());
//    Assert.assertEquals(HttpMethod.GET, copyContext.method());
//    Assert.assertEquals("/devices", copyContext.path());
//    JsonObject principal = copyContext.principal();
//    Assert.assertEquals("edgar", principal.getString("userId"));
//
//    System.out.println(copyContext);
//  }
//
//  @Test
//  public void testCopyDefinition() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null, null, null);
//
//    Endpoint httpEndpoint = HttpEndpoint.http("add_device", HttpMethod.POST, "/devices",
//        "device");
//    ApiDefinition apiDefinition = ApiDefinition.create("add_device", HttpMethod.POST, "/devices",
//        Lists.newArrayList(httpEndpoint));
//    apiContext.setApiDefinition(apiDefinition);
//
//    ApiContext copyContext = apiContext.copy();
//    Assert.assertEquals(apiContext.id(), copyContext.id());
//    Assert.assertEquals(HttpMethod.GET, copyContext.method());
//    Assert.assertEquals("/devices", copyContext.path());
//    ApiDefinition copyDefinition = copyContext.apiDefinition();
//    Assert.assertEquals("/devices", copyDefinition.path());
//    Assert.assertEquals("add_device", copyDefinition.name());
//    Assert.assertEquals(HttpMethod.POST, copyDefinition.method());
//    Assert.assertEquals(1, copyDefinition.endpoints().size());
//  }
//
//  @Test
//  public void testGetValueByKeywordFromHeader() {
//    Multimap<String, String> header = ArrayListMultimap.create();
//    header.put("h1", "h1.1");
//    header.put("h1", "h1.2");
//    header.put("h2", "h2");
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", header,
//            null, null);
//    Object value = apiContext.getValueByKeyword("$header.h1");
//    Assert.assertTrue(value instanceof List);
//    Assert.assertEquals(2, List.class.cast(value).size());
//
//    value = apiContext.getValueByKeyword("$header.h2");
//    Assert.assertEquals("h2", value);
//    value = apiContext.getValueByKeyword("$header.undefined");
//    Assert.assertNull(value);
//  }
//
//  @Test
//  public void testGetValueByKeywordFromParam() {
//    Multimap<String, String> params = ArrayListMultimap.create();
//    params.put("p1", "p1.1");
//    params.put("p1", "p1.2");
//    params.put("p2", "p2");
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null,
//            params, new JsonObject().put("foo", "bar"));
//    Object value = apiContext.getValueByKeyword("$query.p1");
//    Assert.assertTrue(value instanceof List);
//    Assert.assertEquals(2, List.class.cast(value).size());
//
//    value = apiContext.getValueByKeyword("$query.p2");
//    Assert.assertEquals("p2", value);
//    value = apiContext.getValueByKeyword("$query.undefined");
//    Assert.assertNull(value);
//  }
//
//  @Test
//  public void testGetValueByKeywordFromBody() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null,
//            null, new JsonObject().put("foo", "bar"));
//
//    Object value = apiContext.getValueByKeyword("$body.foo");
//    Assert.assertEquals("bar", value);
//    value = apiContext.getValueByKeyword("$body.undefined");
//    Assert.assertNull(value);
//
//    apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null,
//            null, null);
//
//    value = apiContext.getValueByKeyword("$body.foo");
//    Assert.assertNull(value);
//
//  }
//
//  @Test
//  public void testGetValueByKeywordFromUser() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null,
//            null, null);
//    apiContext.setPrincipal(new JsonObject().put("userId", 1));
//
//    Object value = apiContext.getValueByKeyword("$user.userId");
//    Assert.assertEquals(1, value);
//    value = apiContext.getValueByKeyword("$user.undefined");
//    Assert.assertNull(value);
//
//    apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null,
//            null, null);
//
//    value = apiContext.getValueByKeyword("$user.userId");
//    Assert.assertNull(value);
//
//  }
//
//  @Test
//  public void testGetValueByKeywordFromVariable() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null,
//            null, new JsonObject().put("foo", "bar"));
//    apiContext.addVariable("username", "edgar");
//    Object value = apiContext.getValueByKeyword("$var.username");
//    Assert.assertEquals("edgar", value);
//    value = apiContext.getValueByKeyword("$var.undefined");
//    Assert.assertNull(value);
//  }
//
//  @Test
//  public void testGetValueByKeywordFromUndefinedKey() {
//    ApiContext apiContext = ApiContext
//        .create(HttpMethod.GET, "/devices", null,
//            null, new JsonObject().put("foo", "bar"));
//    apiContext.addVariable("username", "edgar");
//    Object value = apiContext.getValueByKeyword("$test.username");
//    Assert.assertEquals("$test.username", value);
//  }
//
//}
