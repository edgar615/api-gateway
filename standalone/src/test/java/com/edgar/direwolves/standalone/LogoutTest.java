package com.edgar.direwolves.standalone;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
public class LogoutTest {
  public static void main(String[] args) {
    String token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJqdGkiOiIxNTgwZDRiNC00NTBiLTQwNzAtOWFhMS1lY2Q4YzEzMzcyYzIiLCJ1c2VySWQiOiItMTg4IiwiaWF0IjoxNDkxMDMyMjA5LCJleHAiOjE0OTEwMzQwMDksImF1ZCI6IiIsImlzcyI6IiIsInN1YiI6IiJ9.nOeOchZ3EzyyMWRgAreWQjw-XmuiwjAuX2DLEA04saFdOdt9IjH92sjCKHzE5pdQcGXxrBzFcdYh77qW8CvzeA==";

    Api api = new Api();
    JsonObject data = new JsonObject();
    api.setData(data.getMap());
    Vertx.vertx().createHttpClient().post(9000, "localhost", "/logout?" + api.signTopRequest())
            .handler(resp -> {
              System.out.println(resp.statusCode());
              resp.bodyHandler(body -> System.out.println(body.toString()));
            })
            .putHeader("Authorization", "Bearer " + token)
            .setChunked(true)
            .end(data.encode());
  }
}
