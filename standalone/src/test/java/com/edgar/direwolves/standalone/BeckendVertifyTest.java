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
            .put("sign", "eyJleHAiOjE0OTQyMTU5ODd9.D02265F94343FF1662EA22841E09F5AC")
            .put("code", "694892");
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
