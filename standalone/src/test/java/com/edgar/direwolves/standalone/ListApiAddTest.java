package com.edgar.direwolves.standalone;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
public class ListApiAddTest {
  public static void main(String[] args) {
    String token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhdWQiOiIiLCJzdWIiOiIiLCJpc3MiOiIiLCJleHAiOjE0ODY1MjY1NTAsInVzZXJJZCI6NjksImlhdCI6MTQ4NjUyNDc1MCwianRpIjoiNmQ0MTc4ODAtOGVmOC00ZTU4LTliMTItMTFjZmFhZmVjMGExIn0=.2ZPPVDN3lp2GmkcFEvVS6Bo4Lf9qWiTMEJDJGMGXV_6scofoCtaKLKZdQM6e3cTBJWuoQK0v18_3eghu0xsTgA==";

    Api api = new Api();
    JsonObject data = new JsonObject()
            .put("start", 2)
            .put("limit", 2);
    api.setData(data.getMap());
    Vertx.vertx().createHttpClient().post(9000, "localhost", "/backend/api.list?" + api
            .signTopRequest())
            .handler(resp -> {
              System.out.println(resp.statusCode());
              resp.bodyHandler(body -> System.out.println(body.toJsonArray().encodePrettily()));
            })
            .putHeader("Authorization", "Bearer " + token)
            .setChunked(true)
            .end(data.encode());
  }
}
