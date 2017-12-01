package com.github.edgar615.direwolves.core.eventbus;

import com.google.common.base.Strings;

import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.log.LogType;
import com.github.edgar615.util.validation.ValidationException;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Edgar on 2017/7/5.
 *
 * @author Edgar  Date 2017/7/5
 */
public class EventbusUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventbusUtils.class);

  private EventbusUtils() {
    throw new AssertionError("Not instantiable: " + EventbusUtils.class);
  }

  public static void onFailure(Message<JsonObject> received, long duration, Throwable throwable) {
    final String id = received.headers().get("x-request-id");
    Log.create(LOGGER)
            .setTraceId(id)
            .setLogType(LogType.SES)
            .setEvent(received.headers().get("x-request-address") + ".reply")
            .setThrowable(throwable)
            .setMessage("[{}ms]")
            .addArg(duration)
            .error();

    if (throwable instanceof SystemException) {
      SystemException ex = (SystemException) throwable;
      received.fail(ex.getErrorCode().getNumber(), ex.getMessage());
    } else if (throwable instanceof ValidationException) {
      received.fail(DefaultErrorCode.INVALID_ARGS.getNumber(),
                    DefaultErrorCode.INVALID_ARGS.getMessage());
    } else {
      received.fail(999, throwable.getMessage());
    }
  }

  public static void reply(Message<JsonObject> received, JsonObject reply, long duration) {
    final String id = received.headers().get("x-request-id");
    DeliveryOptions options = new DeliveryOptions();
    if (!Strings.isNullOrEmpty(id)) {
      options.addHeader("x-request-id", id);
    }
    final String address = received.headers().get("x-request-address");
    if (!Strings.isNullOrEmpty(address)) {
      options.addHeader("x-request-address", received.headers().get("x-request-address"));
    }

    Log.create(LOGGER)
            .setTraceId(id)
            .setLogType(LogType.SES)
            .setEvent(received.headers().get("x-request-address") + ".reply")
//            .addData("message", reply.encode())
            .setMessage("[{}ms] [{}bytes]")
            .addArg(duration)
            .addArg(reply.toString().getBytes().length)
            .info();
    received.reply(reply, options);
  }
}
