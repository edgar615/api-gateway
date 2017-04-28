package com.edgar.direwolves.dispatch.handler;

import com.edgar.direwolves.core.utils.Helper;
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
      failureMsg.mergeIn(new JsonObject(ex.asMap()));

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
      LOGGER.error("---| [{}] [ERROR] [{}]", id,
                  throwable.getMessage(),
                  throwable
      );
      printErrorMsg = true;
      SystemException ex = SystemException.wrap(DefaultErrorCode.UNKOWN, throwable)
              .set("details", throwable.getMessage());
      statusCode = ex.getErrorCode().getStatusCode();
      failureMsg.mergeIn(new JsonObject(ex.asMap()));
    }

    if (printErrorMsg) {
      Helper.logError(LOGGER, id, FailureHandler.class.getSimpleName(),
                      "encode exception: " + failureMsg.encode(),
                      throwable);
    } else {
      Helper.logFailed(LOGGER, id, FailureHandler.class.getSimpleName(),
                       "encode exception: " + failureMsg.encode());
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
        jsonObject.put("code", DefaultErrorCode.EVENTBUS_NO_HANDLERS.getNumber())
                .put("message", DefaultErrorCode.EVENTBUS_NO_HANDLERS.getMessage());
      } else if (replyFailure == ReplyFailure.TIMEOUT) {
        jsonObject.put("code", DefaultErrorCode.EVENTBUS_TIMOUT.getNumber())
                .put("message", DefaultErrorCode.EVENTBUS_TIMOUT.getMessage());
      } else if (replyFailure == ReplyFailure.RECIPIENT_FAILURE) {
        jsonObject.put("code", DefaultErrorCode.EVENTBUS_REJECTED.getNumber())
                .put("message", DefaultErrorCode.EVENTBUS_REJECTED.getMessage());
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