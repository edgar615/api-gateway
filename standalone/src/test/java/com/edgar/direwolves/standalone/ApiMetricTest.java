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
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJqdGkiOiIxZTRkNjIxNi1hYmI5LTQyYTctOGUwYi04NTc5MzNhZjI4NjAiLCJ1c2VySWQiOi0xODgsImlhdCI6MTQ5MTAyNjQzMywiZXhwIjoxNDkxMDI4MjMzLCJhdWQiOiIiLCJpc3MiOiIiLCJzdWIiOiIifQ==.y1bKZVZ4Z6YqW1I0gg6L3yJ3VBjdsLWPfrVcnmgS_itoIW3Pd0kZPHW3zpZNV39kFFF-yGXAVZeplbh3wWmnhA==";

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
