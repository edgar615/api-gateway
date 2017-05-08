package com.edgar.direwolves.standalone;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
public class ApiMetricTest {
  public static void main(String[] args) {
    String token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJqdGkiOiI1NGM4MzdhYS03MzQ1LTQ5MWYtYWQyNi0zOTQ2NmVhMTQ0NTMiLCJ1c2VySWQiOiItMTg4IiwiaWF0IjoxNDk0MjE1NzEwLCJleHAiOjE0OTQyMTc1MTAsImF1ZCI6IiIsImlzcyI6IiIsInN1YiI6IiJ9.OKtwjyZCMrCNd16_bGhcWj2QP7OS4nc8_7AseVo73svQgXJTV760MTy0JOPCMT51AGDNLAlgzCbcc9-LL0wReg==";

    Api api = new Api();
    JsonObject data = new JsonObject()
            .put("name", "example")
            .put("start", 2)
            .put("limit", 2);
    api.setData(data.getMap());
    Vertx.vertx().createHttpClient().post(9000, "localhost", "/backend/api.metric?" + api
            .signTopRequest())
            .handler(resp -> {
              System.out.println(resp.statusCode());
              resp.bodyHandler(body -> System.out.println(body.toJsonObject().encodePrettily()));
            })
            .putHeader("Authorization", "Bearer " + token)
            .setChunked(true)
            .end(data.encode());
  }
}
