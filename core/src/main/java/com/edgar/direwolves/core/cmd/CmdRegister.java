package com.edgar.direwolves.core.cmd;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.edgar.direwolves.core.eventbus.EventbusUtils;
import com.edgar.direwolves.core.utils.Log;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import com.edgar.util.vertx.eventbus.Event;
import com.edgar.util.vertx.eventbus.EventUtils;
import com.edgar.util.vertx.spi.Initializable;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;
import java.util.UUID;

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
                      .setModule("api.cmd")
                      .setEvent("cmd.register")
                      .addData("address", address)
                      .addData("cmd", cmd.cmd())
                      .info();

              eb.<Event>consumer(address, msg -> consumer(cmd, msg));
            });
    complete.complete();
  }

  public void consumer(ApiCmd cmd,  Message<Event> msg) {
    Event event = msg.body();
    Log.create(LOGGER)
            .setTraceId(event.id())
            .setModule("api.cmd")
            .setEvent(cmd.cmd()+ ".received")
            .addData("event", event)
            .info();

    long started = System.currentTimeMillis();
    Future<JsonObject> future = cmd.handle(event.body().put("traceId", event.id()));
    future.setHandler(ar -> {
      long duration = System.currentTimeMillis() - started;
      if (ar.succeeded()) {
        int bytes;
        if (ar.result() == null) {
          bytes = 0;
        } else {
          bytes = ar.result().toString().getBytes().length;
        }
        Event response = Event.builder()
                .setReplyTo(event.id())
                .setAddress(msg.replyAddress())
                .setBody(ar.result())
                .build();
        Log.create(LOGGER)
                .setTraceId(event.id())
                .setModule("api.cmd")
                .setEvent(cmd.cmd() +".reply")
                .addData("event", response)
                .setMessage("{}ms; {}bytes")
                .addArg(duration)
                .addArg(bytes)
                .info();
        msg.reply(response);
      } else {
        Log.create(LOGGER)
                .setTraceId(event.id())
                .setModule("api.cmd")
                .setEvent(cmd.cmd() +".reply")
                .setThrowable(ar.cause())
                .setMessage("{}ms")
                .addArg(duration)
                .error();
        EventbusUtils.onFailure(msg, ar.cause());
      }
    });
  }

  private String cmdAddress( String cmd) {
    return "direwolves.eb." + cmd;
  }

}
