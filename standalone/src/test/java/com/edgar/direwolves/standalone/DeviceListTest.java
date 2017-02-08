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
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiIiLCJzdWIiOiIiLCJpc3MiOiIiLCJleHAiOjE0ODY1NDc0MTIsInVzZXJJZCI6NjksImlhdCI6MTQ4NjU0NTYxMiwianRpIjoiZjkzYTFiMjItNzg0Ny00ZGZmLTliYmMtMTY1YWNlNzA2YjFiIn0=.c52uaniWbM0TDn28Hs63DhL2-zJAaF9yljQZN7PO4v4Ib6yYVVfBklK5IVdvHHiorA-6i19x079xvj_rBN2DKw==";

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
