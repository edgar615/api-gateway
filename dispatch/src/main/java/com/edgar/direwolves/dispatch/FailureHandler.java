package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.core.utils.Log;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Map;
import java.util.UUID;

/**
 * 异常的处理类.
 *
 * @author Edgar  Date 2016/2/18
 */
public class FailureHandler implements Handler<RoutingContext> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FailureHandler.class);

  public static Handler<RoutingContext> create() {
    return new FailureHandler();
  }

  public static void doHandle(RoutingContext rc, Throwable throwable) {
    String id = (String) rc.data().getOrDefault("x-request-id", UUID.randomUUID().toString());
    int statusCode = 400;
    boolean printErrorMsg = false;
    JsonObject failureMsg = new JsonObject();
    HttpServerResponse response = rc.response();
    if (throwable instanceof SystemException) {
      SystemException ex = (SystemException) throwable;
      statusCode = ex.getErrorCode().getStatusCode();
      //resp.header:开头的错误信息表示需要在响应头上显示，而不是在响应体中显示
      for (Map.Entry<String, Object> entry : ex.asMap().entrySet()) {
        if (entry.getKey().startsWith("resp.header:")) {
          response.putHeader(entry.getKey().substring("resp.header:".length()),
                             entry.getValue().toString());
        } else {
          failureMsg.put(entry.getKey(), entry.getValue());
        }
      }
    } else if (throwable instanceof ValidationException) {
      SystemException ex = SystemException.create(DefaultErrorCode.INVALID_ARGS);
      ValidationException vex = (ValidationException) throwable;
      if (!vex.getErrorDetail().isEmpty()) {
        ex.set("details", vex.getErrorDetail().asMap());
      }
      statusCode = ex.getErrorCode().getStatusCode();
      failureMsg.mergeIn(new JsonObject(ex.asMap()));
    } else if (throwable instanceof ReplyException) {
      ReplyException ex = (ReplyException) throwable;
      JsonObject jsonObject = replyJson(ex);
      statusCode = 400;
      failureMsg.mergeIn(jsonObject);
    } else if (throwable instanceof ConnectException) {
      ConnectException connectException = (ConnectException) throwable;
      SystemException ex = SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
              .set("details", connectException.getMessage());
      statusCode = ex.getErrorCode().getStatusCode();
      failureMsg.mergeIn(new JsonObject(ex.asMap()));
    } else {
      printErrorMsg = true;
      SystemException ex = SystemException.wrap(DefaultErrorCode.UNKOWN, throwable)
              .set("details", throwable.getMessage());
      statusCode = ex.getErrorCode().getStatusCode();
      failureMsg.mergeIn(new JsonObject(ex.asMap()));
    }

    if (printErrorMsg) {
      Log.create(LOGGER)
              .setTraceId(id)
              .setEvent("http.request.failed")
              .setMessage(failureMsg.encode())
              .setThrowable(throwable)
              .error();
    } else {
      Log.create(LOGGER)
              .setTraceId(id)
              .setEvent("http.request.failed")
              .setMessage(failureMsg.encode())
              .error();
    }
    response.putHeader("x-request-id", id)
            .setStatusCode(statusCode).end(failureMsg.encode());
  }

  private static JsonObject replyJson(ReplyException ex) {
    JsonObject jsonObject = new JsonObject();
    DefaultErrorCode errorCode = DefaultErrorCode.getCode(ex.failureCode());
    if (errorCode != null) {
      jsonObject.put("code", errorCode.getNumber())
              .put("message", errorCode.getMessage());
    } else {
      ReplyFailure replyFailure = ex.failureType();
      if (replyFailure == ReplyFailure.NO_HANDLERS) {
        jsonObject.put("code", DefaultErrorCode.SERVICE_UNAVAILABLE.getNumber())
                .put("message", DefaultErrorCode.SERVICE_UNAVAILABLE.getMessage());
      } else if (replyFailure == ReplyFailure.TIMEOUT) {
        jsonObject.put("code", DefaultErrorCode.TIME_OUT.getNumber())
                .put("message", DefaultErrorCode.TIME_OUT.getMessage());
      } else if (replyFailure == ReplyFailure.RECIPIENT_FAILURE) {
        jsonObject.put("code", ex.failureCode())
                .put("message", ex.getMessage());
      } else {
        jsonObject.put("code", DefaultErrorCode.UNKOWN.getNumber())
                .put("message", DefaultErrorCode.UNKOWN.getMessage());
      }
    }
    return jsonObject;
  }

  @Override
  public void handle(RoutingContext rc) {
    Throwable throwable = rc.failure();
    doHandle(rc, throwable);
  }

}