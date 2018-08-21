package com.github.edgar615.gateway.dispatch;

import com.github.edgar615.gateway.core.eventbus.EventbusUtils;
import com.github.edgar615.gateway.core.utils.Consts;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.StatusBind;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.ValidationException;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.ReplyException;
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

    private final StatusBind statusBind;

    public FailureHandler() {
        this.statusBind = StatusBind.instance().addDefault();
    }

    public static Handler<RoutingContext> create() {
        return new FailureHandler();
    }

    public void doHandle(RoutingContext rc, Throwable throwable) {
        String id = (String) rc.data().getOrDefault("x-request-id", UUID.randomUUID().toString());
        rc.data().put("responsedOn", System.currentTimeMillis());
        JsonObject failureMsg = new JsonObject();
        HttpServerResponse response = rc.response();
        SystemException ex = wrap(throwable);
        int statusCode = statusBind.statusCode(ex.getErrorCode().getNumber());
        //resp.header:开头的错误信息表示需要在响应头上显示，而不是在响应体中显示
        for (Map.Entry<String, Object> entry : ex.asMap().entrySet()) {
            if (entry.getKey().startsWith(Consts.RESPONSE_HEADER)) {
                response.putHeader(entry.getKey().substring(Consts.RESPONSE_HEADER.length()),
                                   entry.getValue().toString());
            } else {
                failureMsg.put(entry.getKey(), entry.getValue());
            }
        }
        if (ex.getErrorCode() == DefaultErrorCode.UNKOWN) {
            LOGGER.error("[{}] [failed] [{}]", id, failureMsg.encode(), throwable);
        } else {
            LOGGER.warn("[{}] [failed] [{}]", id, failureMsg.encode());
        }
        response.putHeader("x-request-id", id)
                .setStatusCode(statusCode).end(failureMsg.encode());
    }

    @Override
    public void handle(RoutingContext rc) {
        Throwable throwable = rc.failure();
        doHandle(rc, throwable);
    }

    private SystemException wrap(Throwable throwable) {
        if (throwable instanceof SystemException) {
            return (SystemException) throwable;
        } else if (throwable instanceof ValidationException) {
            SystemException ex = SystemException.create(DefaultErrorCode.INVALID_ARGS);
            ValidationException vex = (ValidationException) throwable;
            if (!vex.getErrorDetail().isEmpty()) {
                ex.set("details", vex.getErrorDetail().asMap());
            }
            return ex;
        } else if (throwable instanceof ReplyException) {
            return EventbusUtils.reductionSystemException(throwable);
        } else if (throwable instanceof ConnectException) {
            ConnectException connectException = (ConnectException) throwable;
            SystemException ex = SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
                    .set("details", connectException.getMessage());
            return ex;
        }
        return SystemException.wrap(DefaultErrorCode.UNKOWN, throwable)
                .set("details", throwable.getMessage());
    }

}