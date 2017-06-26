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
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJhOWM2ZmYyMS1kOWExLTRhMzUtYjZhNS01ZDdmYzcwOTViZGIiLCJ1c2VySWQiOiItMTg4IiwiaWF0IjoxNDk4NDQ5NTcyLCJleHAiOjE0OTg0NTEzNzIsImF1ZCI6IiIsImlzcyI6IiIsInN1YiI6IiJ9.jStaRRAapQ_Fa3GDTk7KIhhIHqKxjiG2WYecfumfyxOM3HYupwXSgosvAe_Hd_g-AE7yGau-w1ZJd3VEW54CGQ==";

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
