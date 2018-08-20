package com.github.edgar615.gateway.core.eventbus;

import com.google.common.base.Strings;

import com.github.edgar615.util.exception.CustomErrorCode;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.ErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.ValidationException;
import com.github.edgar615.util.vertx.JsonUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
    LOGGER.error("[{}] [SER] [failed] [{}] [{}ms]", id, received.headers().get("x-request-address"),
                 duration, throwable);

    if (throwable instanceof SystemException) {
      SystemException ex = (SystemException) throwable;
      JsonObject jsonObject = new JsonObject(ex.asMap());
      received.fail(ex.getErrorCode().getNumber(), jsonObject.encode());
    } else if (throwable instanceof ValidationException) {
      SystemException ex = SystemException.create(DefaultErrorCode.INVALID_ARGS);
      ValidationException vex = (ValidationException) throwable;
      if (!vex.getErrorDetail().isEmpty()) {
        ex.set("details", vex.getErrorDetail().asMap());
      }
      JsonObject jsonObject = new JsonObject(ex.asMap());
      received.fail(ex.getErrorCode().getNumber(), jsonObject.encode());
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

    LOGGER.info("[{}] [SER] [OK] [{}bytes] [{}ms]", id,
                received.headers().get("x-request-address"),
                reply.toString().getBytes().length, duration);
    received.reply(reply, options);
  }

  public static SystemException reductionSystemException(Throwable throwable) {
    if (throwable instanceof SystemException) {
      return (SystemException) throwable;
    }
    if (throwable instanceof ValidationException) {
      SystemException ex = SystemException.create(DefaultErrorCode.INVALID_ARGS);
      ValidationException vex = (ValidationException) throwable;
      if (!vex.getErrorDetail().isEmpty()) {
        ex.set("details", vex.getErrorDetail().asMap());
      }
      return ex;
    }
    if (throwable instanceof ReplyException) {
      ReplyException ex = (ReplyException) throwable;
      if (ex.failureType() == ReplyFailure.NO_HANDLERS) {
        SystemException systemException =
                SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
                        .set("details", "No handlers");
        return systemException;
      } else if (ex.failureType() == ReplyFailure.TIMEOUT) {
        SystemException systemException =
                SystemException.create(DefaultErrorCode.TIME_OUT);
        return systemException;
      } else {
        //判断是不是JsonObject
        String message = ex.getMessage();
        if (message != null && message.startsWith("{") && message.endsWith("}")) {
          try {
            JsonObject jsonObject = Buffer.buffer(message).toJsonObject();
            String errorMsg = jsonObject.getString("message", "unkown");
            Map<String, Object> properties = JsonUtils.toMap(jsonObject);
            properties.remove("message");
            properties.remove("code");
            ErrorCode errorCode = CustomErrorCode.create(ex.failureCode(), errorMsg);
            return SystemException.create(errorCode).setAll(properties);
          } catch (Exception e) {
            ErrorCode errorCode = CustomErrorCode.create(ex.failureCode(), message);
            return SystemException.create(errorCode);
          }
        }
        if (Strings.isNullOrEmpty(message)) {
          message = "unkown";
        }
        ErrorCode errorCode = CustomErrorCode.create(ex.failureCode(), message);
        return SystemException.create(errorCode);
      }
    }
    return SystemException.wrap(DefaultErrorCode.UNKOWN, throwable);
  }
}
