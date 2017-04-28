package com.edgar.direwolves.standalone;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
public class LoginTest {
  public static void main(String[] args) {
    JsonObject data = new JsonObject()
            .put("username", "18627874615")
            .put("password", "123")
            .put("deviceType", 3)
            .put("channelId", "1");
    Vertx.vertx().createHttpClient().post(9000, "localhost", "/login?v=1.1")
            .handler(resp -> {
              System.out.println(resp.statusCode());
              System.out.println(resp.headers().get("x-server-time"));
              resp.bodyHandler(body -> System.out.println(body.toString()));
            })
            .setChunked(true)
            .end(data.encode());
  }
}
