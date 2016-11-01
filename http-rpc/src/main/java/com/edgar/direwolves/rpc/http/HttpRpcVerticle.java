package com.edgar.direwolves.rpc.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/11/1.
 *
 * @author Edgar  Date 2016/11/1
 */
public class HttpRpcVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    HttpClient httpClient = vertx.createHttpClient();
    vertx.eventBus().<JsonObject>consumer("direwolves.rpc.http.req", msg -> {
      JsonObject jsonObject = msg.body();
      try {
        HttpRequestOptions options = new HttpRequestOptions(jsonObject);
        Future<HttpResult> future = Http.request(httpClient, options);
        future.setHandler(ar -> {
          if (ar.succeeded()) {
            msg.reply(ar.result().toJson());
          } else {
            msg.fail(-1, ar.cause().getMessage());
          }
        });
      } catch (Exception e) {
        msg.fail(-1, e.getMessage());
      }

    });
  }
}
