package com.edgar.direwolves.example;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
public class DeviceListTest {
  public static void main(String[] args) {
    String token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiIiLCJzdWIiOiIiLCJpc3MiOiIiLCJleHAiOjE0ODY0NjEyNDUsInVzZXJJZCI6NjksImlhdCI6MTQ4NjQ1OTQ0NSwianRpIjoiMGYyN2JmYmItNmU4ZC00OTAxLTk4NDUtODgxNmI4OTNmMTI0In0=.aP2AIO3W28gLaITfHJvw4M11kic5BAx_W7iGshgFKm50YlJNupGdIlxRDy_noxw2bbQLVo2VjOU5enG4d4c1ag==";

    Api api = new Api();
    Vertx.vertx().createHttpClient().get(9000, "localhost", "/devices?"+ api.signTopRequest())
            .handler(resp -> {
              System.out.println(resp.statusCode());
              resp.bodyHandler(body -> System.out.println(body.toString()));
            })
            .putHeader("Authorization", "Bearer " + token)
            .setChunked(true)
            .end();
  }
}
