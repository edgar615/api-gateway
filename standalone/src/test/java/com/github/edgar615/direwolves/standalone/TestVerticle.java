package com.github.edgar615.direwolves.standalone;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/11/24.
 *
 * @author Edgar  Date 2017/11/24
 */
public class TestVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("event.user.keepalive", msg -> {
      System.out.println(msg.body());
    });
    vertx.eventBus().consumer("event.user.location", msg -> {
      System.out.println(msg.body());
    });
    vertx.eventBus().consumer("job.user.login", msg -> {
      System.out.println(msg.body());
      msg.reply(new JsonArray());
    });
  }
}
