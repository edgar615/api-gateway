package com.edgar.direwolves.verticle;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.cmd.ApiCmdFactory;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
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
public class RegisterApiCmd implements Initializable {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterApiCmd.class);

  @Override
  public void initialize(Vertx vertx, JsonObject config, Future<Void> complete) {
    String namespace = config.getString("project.namespace", "");
    //eventbus consumer
    EventBus eb = vertx.eventBus();
    Lists.newArrayList(ServiceLoader.load(ApiCmdFactory.class))
            .stream()
            .map(f -> f.create(vertx, config))
            .forEach(cmd -> {
              String address = cmdAddress(namespace, cmd.cmd());
              LOGGER.info("---| [Register consumer] [{}]", address);
              eb.<JsonObject>consumer(address, msg -> consumer(cmd, address, msg));
            });
    complete.complete();
  }

  public void consumer(ApiCmd cmd, String address, Message<JsonObject> msg) {
    JsonObject param = msg.body();
    MultiMap headers = msg.headers();
    String id = headers.get("x-request-id");
    long started = System.currentTimeMillis();
    if (Strings.isNullOrEmpty(id)) {
      id = UUID.randomUUID().toString();
    }
    LOGGER.info("===> [{}] [{}] [{}] [{}]",
                id,
                address,
                convertToString(headers, "no header"),
                param.encode());
    Future<JsonObject> future = cmd.handle(param);
    final String finalId = id;
    future.setHandler(ar -> {
      long duration = System.currentTimeMillis() - started;
      if (ar.succeeded()) {
        LOGGER.info("<=== [{}] [OK] [{}ms] [{} bytes]", finalId,
                    duration,
                    ar.result().toString().getBytes().length);
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

  private String cmdAddress(String namespace, String cmd) {
    if (Strings.isNullOrEmpty(namespace)) {
      return "direwolves.eb." + cmd;
    }
    return namespace + ".direwolves.eb." + cmd;
  }

  private void eventbusFailureHandler(Message<JsonObject> msg, Throwable throwable) {
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
