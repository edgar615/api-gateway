package com.edgar.direwolves.core.cmd;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.edgar.direwolves.core.utils.LoggerUtils;
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
              LoggerUtils.info(LOGGER, "cmd.registered", "register consumer",
                               Lists.newArrayList("address"),
                               Lists.newArrayList(address));

              eb.<Event>consumer(address, msg -> consumer(cmd, address, msg));
            });
    complete.complete();
  }

  public void consumer(ApiCmd cmd, String address, Message<Event> msg) {
    Event event = msg.body();
    MultiMap headers = msg.headers();
    String id = headers.get("x-request-id");
    long started = System.currentTimeMillis();
    if (Strings.isNullOrEmpty(id)) {
      id = UUID.randomUUID().toString();
    }
    EventUtils.inLog(LOGGER, event);
    Future<JsonObject> future = cmd.handle(event.body());
    final String finalId = id;
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
        EventUtils.outLog(LOGGER, response, "response");
        msg.reply(ar.result());
      } else {
        LOGGER.error("<===  [{}] [FAILED] [{}ms] [{}]", finalId,
                     duration,
                     ar.cause().getMessage(),
                     ar.cause());
        eventbusFailureHandler(msg, ar.cause());
      }
    });
  }

  /**
   * 将Multimap转换为字符串用来记录日志.
   *
   * @param multimap      Multimap
   * @param defaultString 如果Multimap是空，返回的默认字符串
   * @return 字符串
   */
  private String convertToString(MultiMap multimap, String defaultString) {
    StringBuilder s = new StringBuilder();
    for (String key : multimap.names()) {
      s.append(key)
              .append(":")
              .append(Joiner.on(",").join(multimap.getAll(key)))
              .append(";");
    }
    if (s.length() == 0) {
      return defaultString;
    }
    return s.toString();
  }

  private String cmdAddress( String cmd) {
    return "direwolves.eb." + cmd;
  }

  private void eventbusFailureHandler(Message<Event> msg, Throwable throwable) {
    if (throwable instanceof SystemException) {
      SystemException ex = (SystemException) throwable;
      msg.fail(ex.getErrorCode().getNumber(), ex.getMessage());
    } else if (throwable instanceof ValidationException) {
      msg.fail(DefaultErrorCode.INVALID_ARGS.getNumber(),
               DefaultErrorCode.INVALID_ARGS.getMessage());
    } else {
      msg.fail(999, throwable.getMessage());
    }
  }
}
