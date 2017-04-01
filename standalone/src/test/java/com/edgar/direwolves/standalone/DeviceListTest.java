package com.edgar.direwolves.standalone;

import io.vertx.core.Vertx;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
public class DeviceListTest {
  public static void main(String[] args) {
    String token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiIiLCJzdWIiOiIiLCJpc3MiOiIiLCJleHAiOjE0ODY5NTMxMDEsInVzZXJJZCI6NjksImlhdCI6MTQ4Njk1MTMwMSwianRpIjoiOTIzM2UxMmYtNmFiNy00ZjNlLWIzYzctZTc5Y2U1NGEzOWI1In0=.UUc6D4FlKoGV_U3tBqZjtXG3kygSWGWLoHTSZcRUIRpWonnXuAlG8wlKRBe-97ycJkfYL2fH_gyM1LKAkxiTCg==";

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
