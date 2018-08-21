package com.github.edgar615.gateway.standalone;

import com.github.edgar615.gateway.core.eventbus.EventbusUtils;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
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
        vertx.eventBus().<JsonObject>consumer("job.appKey.get", msg -> {
            System.out.println(msg.body());
            String appKey = msg.body().getString("appKey");
            if ("pyuywmyijucuzlfkhxvs".equalsIgnoreCase(appKey)) {
                JsonObject jsonObject = new JsonObject()
                        .put("appKey", "pyuywmyijucuzlfkhxvs")
                        .put("appSecret", "5416cc11b35d403bba9505a05954517a")
                        .put("clientCode", 100)
                        .put("permissions", new JsonArray().add("all"));
                EventbusUtils.reply(msg, jsonObject, 0);
            } else {
                SystemException systemException =
                        SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                                .set("foo", "bar");
                EventbusUtils.onFailure(msg, 0, systemException);
            }
        });

        vertx.eventBus().<JsonObject>consumer("job.user.get", msg -> {
            System.out.println(msg.body());
            String userId = msg.body().getString("userId");
            if ("1".equalsIgnoreCase(userId)) {
                JsonObject jsonObject = new JsonObject()
                        .put("userId", 1)
                        .put("username", "edgar")
                        .put("fullname", "edgar615")
                        .put("permissions", new JsonArray().add("all"));
                EventbusUtils.reply(msg, jsonObject, 0);
            } else {
                SystemException systemException =
                        SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                                .set("foo", "bar");
                EventbusUtils.onFailure(msg, 0, systemException);
            }
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
