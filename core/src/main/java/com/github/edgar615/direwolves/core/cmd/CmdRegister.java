package com.github.edgar615.direwolves.core.cmd;

import com.google.common.collect.Lists;

import com.github.edgar615.direwolves.core.eventbus.EventbusUtils;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.log.LogType;
import com.github.edgar615.util.vertx.spi.Initializable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * Created by Edgar on 2017/3/30.
 *
 * @author Edgar  Date 2017/3/30
 */
public class CmdRegister implements Initializable {
  private static final Logger LOGGER = LoggerFactory.getLogger(CmdRegister.class);

  @Override
  public void initialize(Vertx vertx, JsonObject config, Future<Void> complete) {
    //eventbus consumer
    EventBus eb = vertx.eventBus();
    Lists.newArrayList(ServiceLoader.load(ApiCmdFactory.class))
            .stream()
            .map(f -> f.create(vertx, config))
            .forEach(cmd -> {
              String address = cmdAddress(cmd.cmd());
              Log.create(LOGGER)
                      .setLogType("ApiDiscovery")
                      .setEvent("register")
                      .addData("address", cmd.cmd())
                      .info();

              eb.<JsonObject>consumer(address, msg -> {
                try {
                  consumer(cmd, msg);
                } catch (Exception e) {
                  EventbusUtils.onFailure(msg, 0, e);
                }
              });
            });
    complete.complete();
  }

  public void consumer(ApiCmd cmd, Message<JsonObject> received) {
    JsonObject message = received.body();
    final String id = received.headers().get("x-request-id");
    Log.create(LOGGER)
            .setTraceId(id)
            .setLogType(LogType.SER)
            .setEvent(cmd.cmd())
            .addData("message", message)
            .info();
    long started = System.currentTimeMillis();
    Future<JsonObject> future = cmd.handle(message.put("traceId", id));
    future.setHandler(ar -> {
      long duration = System.currentTimeMillis() - started;
      if (ar.succeeded()) {
        JsonObject reply = ar.result();
        if (reply == null) {
          reply = new JsonObject();
        }
        EventbusUtils.reply(received, reply, duration);
      } else {
        EventbusUtils.onFailure(received, duration, ar.cause());
      }
    });
  }

  private String cmdAddress(String cmd) {
    return cmd;
  }

}
