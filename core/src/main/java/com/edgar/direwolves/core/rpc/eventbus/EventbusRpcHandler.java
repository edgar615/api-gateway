package com.edgar.direwolves.core.rpc.eventbus;

import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.direwolves.core.utils.Helper;
import com.edgar.direwolves.core.utils.MultimapUtils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Edgar on 2016/12/30.
 *
 * @author Edgar  Date 2016/12/30
 */
public class EventbusRpcHandler implements RpcHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventbusRpcHandler.class);

  private final Vertx vertx;

  EventbusRpcHandler(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public String type() {
    return EventbusEndpoint.TYPE;
  }

  @Override
  public Future<RpcResponse> handle(RpcRequest rpcRequest) {
    EventbusRpcRequest request = (EventbusRpcRequest) rpcRequest;

    LOGGER.info("------> [{}] [{}] [{}] [{}] [{}] [{}]",
                request.id(),
                type().toUpperCase(),
                request.policy(),
                request.address(),
                MultimapUtils.convertToString(request.headers(), "no header"),
                request.message() == null ? "no body" : request.message().encode()
    );

    Future<RpcResponse> future = Future.future();
    if (EventbusEndpoint.PUB_SUB.equalsIgnoreCase(request.policy())) {
      pubsub(request, future);
    } else if (EventbusEndpoint.POINT_POINT.equalsIgnoreCase(request.policy())) {
      pointToPoint(request, future);
    } else if (EventbusEndpoint.REQ_RESP.equalsIgnoreCase(request.policy())) {
      reqResp(request, future);
    } else {
      future.fail(SystemException.create(DefaultErrorCode.UNKOWN_REMOTE));
    }

    return future;
  }

  private void pubsub(EventbusRpcRequest request, Future<RpcResponse> completed) {
    DeliveryOptions deliveryOptions = createDeliveryOptions(request);
    vertx.eventBus().publish(request.address(), request.message(), deliveryOptions);
    JsonObject result = new JsonObject()
            .put("result", 1);

    LOGGER.info("<------ [{}] [{}] [{}] [{}ms] [{} bytes]",
                request.id(),
                request.type().toUpperCase(),
                "OK",
                0,
                result.encode().getBytes().length
    );
    completed.complete(RpcResponse.createJsonObject(request.id(), 200, result, 0));
  }

  private DeliveryOptions createDeliveryOptions(EventbusRpcRequest request) {
    DeliveryOptions deliveryOptions = new DeliveryOptions()
            .addHeader("x-request-id", request.id());
    Multimap<String, String> headers = request.headers();
    for (String key : headers.keySet()) {
      for (String value : headers.get(key)) {
        deliveryOptions.addHeader(key, value);
      }
    }
    return deliveryOptions;
  }

  private void pointToPoint(EventbusRpcRequest request, Future<RpcResponse> completed) {
    DeliveryOptions deliveryOptions = createDeliveryOptions(request);
    vertx.eventBus().send(request.address(), request.message(), deliveryOptions);
    JsonObject result = new JsonObject()
            .put("result", 1);
    LOGGER.info("<------ [{}] [{}] [{}] [{}ms] [{} bytes]",
                request.id(),
                request.type().toUpperCase(),
                "OK",
                0,
                result.encode().getBytes().length
    );
    completed.complete(RpcResponse.createJsonObject(request.id(), 200, result, 0));
  }

  private void reqResp(EventbusRpcRequest request, Future<RpcResponse> completed) {
    DeliveryOptions deliveryOptions = createDeliveryOptions(request);
    long srated = System.currentTimeMillis();
    vertx.eventBus().<JsonObject>send(request.address(), request.message(), deliveryOptions, ar -> {
      long elapsedTime = System.currentTimeMillis() - srated;
      if (ar.succeeded()) {
        int bytes;
        if (ar.result() == null) {
          bytes = 0;
        } else {
          bytes = ar.result().toString().getBytes().length;
        }

        LOGGER.info("<------ [{}] [{}] [{}] [{}ms] [{} bytes]",
                    request.id(),
                    request.type().toUpperCase(),
                    "OK",
                    elapsedTime,
                    bytes
        );
        if (ar.result().body() == null) {
          completed.fail(new NullPointerException("result is null"));
          return;
        }
        if (!ar.result().body().containsKey("result")) {
          JsonObject result = ar.result().body();
          completed.complete(RpcResponse.createJsonObject(request.id(), 200, result, elapsedTime));
        } else {
          Object result = ar.result().body().getValue("result");
          if (result instanceof JsonArray) {
            completed.complete(
                    RpcResponse
                            .createJsonArray(request.id(), 200, (JsonArray) result, elapsedTime));
          } else if (result instanceof JsonObject) {
            completed.complete(
                    RpcResponse
                            .createJsonObject(request.id(), 200, (JsonObject) result, elapsedTime));
          } else {
            completed.complete(
                    RpcResponse
                            .createJsonObject(request.id(), 200, ar.result().body(), elapsedTime));
          }

        }
      } else {
        Helper.logFailed(LOGGER, request.id(),
                         this.getClass().getSimpleName(),
                         ar.cause().getMessage());

        if (ar.cause() instanceof ReplyException) {
          ReplyException ex = (ReplyException) ar.cause();
          if (ex.failureType() == ReplyFailure.NO_HANDLERS) {
            SystemException resourceNotFoundEx =
                    SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                            .set("details", "No handlers for address " + request.address());
            completed.fail(resourceNotFoundEx);
          } else {
            completed.fail(ar.cause());
          }
        } else {
          completed.fail(ar.cause());
        }
      }
    });
  }
}
