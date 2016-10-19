package com.edgar.direwolves.definition;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public class HttpEndPointDecoderTest {

  @Test
  public void testDecode() {
    JsonObject jsonObject = new JsonObject()
            .put("type", "http")
            .put("name", "add_device")
            .put("service", "device")
            .put("method", "post")
            .put("path", "/devices");
//            .put("request.header.remove", new JsonArray().add("h3").add("h4"))
//            .put("request.query.remove", new JsonArray().add("q3").add("q4"))
//            .put("request.body.remove", new JsonArray().add("p3").add("p4"))
//            .put("request.header.replace", new JsonArray().add("h5:v2").add("h6:v1"))
//            .put("request.query.replace", new JsonArray().add("q5:v2").add("q6:v1"))
//            .put("request.body.replace", new JsonArray().add("p5:v2").add("p6:v1"))
//            .put("request.header.add", new JsonArray().add("h1:v2").add("h2:v1"))
//            .put("request.query.add", new JsonArray().add("q1:v2").add("q2:v1"))
//            .put("request.body.add", new JsonArray().add("p1:v2").add("p2:v1"));
    HttpEndpoint httpEndpoint = HttpEndpoint.fromJson(jsonObject);
    Assert.assertEquals("/devices", httpEndpoint.path());
//    Assert.assertEquals(2, httpEndpoint.reqHeadersRemove().size());
//    Assert.assertEquals(2, httpEndpoint.reqUrlArgsRemove().size());
//    Assert.assertEquals(2, httpEndpoint.reqBodyArgsRemove().size());
//    Assert.assertEquals(2, httpEndpoint.reqHeadersReplace().size());
//    Assert.assertEquals(2, httpEndpoint.reqUrlArgsReplace().size());
//    Assert.assertEquals(2, httpEndpoint.reqBodyArgsReplace().size());
//    Assert.assertEquals(2, httpEndpoint.reqHeadersAdd().size());
//    Assert.assertEquals(2, httpEndpoint.reqUrlArgsAdd().size());
//    Assert.assertEquals(2, httpEndpoint.reqBodyArgsAdd().size());
  }
}
