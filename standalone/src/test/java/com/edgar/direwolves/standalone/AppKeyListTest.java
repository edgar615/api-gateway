package com.edgar.direwolves.standalone;

import io.vertx.core.Vertx;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
public class AppKeyListTest {
  public static void main(String[] args) {
    Api api = new Api();
    Vertx.vertx().createHttpClient().get(9000, "localhost", "/companies?"+ api.signTopRequest())
            .handler(resp -> {
              System.out.println(resp.statusCode());
              resp.bodyHandler(body -> System.out.println(body.toString()));
            })
            .setChunked(true)
            .end();
  }
}
