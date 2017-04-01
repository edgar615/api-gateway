package com.edgar.direwolves.standalone;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
public class BeckendVertifyTest {
  public static void main(String[] args) {

    Api api = new Api();
    JsonObject data = new JsonObject()
            .put("username", "18627874615")
            .put("sign", "eyJleHAiOjE0OTEwMjY3MTB9.5C067F0DA21B46FDD14C8C6F22EB0E19")
            .put("code", "734335");
    api.setData(data.getMap());
    Vertx.vertx().createHttpClient()
            .post(9000, "localhost", "/backend/token?" + api.signTopRequest())
            .handler(resp -> {
              System.out.println(resp.statusCode());
              resp.bodyHandler(body -> System.out.println(body.toString()));
            })
            .setChunked(true)
            .end(data.encode());
  }
}
