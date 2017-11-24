package com.github.edgar615.direwolves.standalone;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

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
    vertx.createHttpServer()
            .requestHandler(req -> {
              req.response().setChunked(true)
                      .end(new JsonObject().put("result", "OK").encode());
            }).listen(10000);

//    MockConsulHttpVerticle mockConsulHttpVerticle = new MockConsulHttpVerticle();
//    mockConsulHttpVerticle.addService(new JsonObject()
//                                              .put("ID", UUID.randomUUID().toString())
//                                              .put("Node", "u221")
//                                              .put("Address", "localhost")
//                                              .put("ServiceID", "u221:device:10000")
//                                              .put("ServiceName", "device")
//                                              .put("ServiceTags", new JsonArray())
//                                              .put("ServicePort", 10000));
//    vertx.deployVerticle(mockConsulHttpVerticle);
  }
}
