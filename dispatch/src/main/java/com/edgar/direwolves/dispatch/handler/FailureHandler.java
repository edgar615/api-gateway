package com.edgar.direwolves.dispatch.handler;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Override
    public void handle(RoutingContext rc) {
        Throwable throwable = rc.failure();
        LOGGER.warn("error: {}, {}", throwable.getClass(), throwable.getMessage());
        handler(rc, throwable);
    }

    public static void handler(RoutingContext rc, Throwable throwable) {

        int statusCode = rc.statusCode();
        HttpServerResponse response = rc.response();
        if (throwable == null) {
            response.setStatusCode(statusCode).end(Buffer.buffer());
        } else if (throwable instanceof SystemException) {
            SystemException ex = (SystemException) throwable;
            JsonObject jsonObject = new JsonObject(ex.asMap());
            response.setStatusCode(ex.getErrorCode().getStatusCode()).end(jsonObject.encode());
        } else if (throwable instanceof ValidationException) {
            SystemException ex = SystemException.create(DefaultErrorCode.INVALID_ARGS);
            ValidationException vex = (ValidationException) throwable;
            ex.set("details", vex.getErrorDetail().asMap());
            JsonObject jsonObject = new JsonObject(ex.asMap());
            response.setStatusCode(ex.getErrorCode().getStatusCode()).end(jsonObject.encode());
        } else {
            SystemException ex = SystemException.wrap(DefaultErrorCode.UNKOWN, throwable)
                    .set("exception", throwable.getMessage());
            JsonObject jsonObject = new JsonObject(ex.asMap());
            response.setStatusCode(ex.getErrorCode().getStatusCode()).end(jsonObject.encode());
        }
    }

}