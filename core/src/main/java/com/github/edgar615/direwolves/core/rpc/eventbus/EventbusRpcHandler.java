package com.github.edgar615.direwolves.core.rpc.eventbus;

import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.EventbusEndpoint;
import com.github.edgar615.direwolves.core.rpc.RpcHandler;
import com.github.edgar615.direwolves.core.rpc.RpcRequest;
import com.github.edgar615.direwolves.core.rpc.RpcResponse;
import com.github.edgar615.direwolves.core.utils.Log;
import com.github.edgar615.direwolves.core.utils.LogType;
import com.github.edgar615.direwolves.core.utils.MultimapUtils;
import com.github.edgar615.util.base.StringUtils;
import com.github.edgar615.util.exception.CustomErrorCode;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.ErrorCode;
import com.github.edgar615.util.exception.SystemException;
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
    DeliveryOptions deliveryOptions = createDeliveryOptions(request);
    JsonObject message = creeateMessage(request);
    Log.create(LOGGER)
            .setTraceId(request.id())
            .setLogType(LogType.CES)
            .setEvent(type().toUpperCase())
            .addData("address", request.address())
            .setMessage("[{}] [{}]")
            .addArg(MultimapUtils.convertToString(request.headers(), "no header"))
            .addArg(request.message() == null ? "no body" : request.message().encode())
            .info();

    Future<RpcResponse> future = Future.future();
    if (EventbusEndpoint.PUB_SUB.equalsIgnoreCase(request.policy())) {
      pubsub(message, deliveryOptions, future);
    } else if (EventbusEndpoint.POINT_POINT.equalsIgnoreCase(request.policy())) {
      pointToPoint(message, deliveryOptions, future);
    } else if (EventbusEndpoint.REQ_RESP.equalsIgnoreCase(request.policy())) {
      reqResp(message, deliveryOptions, future);
    } else {
      future.fail(SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE));
    }

    return future;
  }

  private void pubsub(JsonObject message, DeliveryOptions options, Future<RpcResponse> completed) {

    vertx.eventBus().publish(options.getHeaders().get("x-request-address"), message, options);
    JsonObject result = new JsonObject()
            .put("result", 1);

    String id = options.getHeaders().get("x-request-id");
    logSuccess("pub-sub", id, 0, result.encode().getBytes().length);
    completed.complete(
            RpcResponse.createJsonObject(options.getHeaders().get("x-request-id"), 200, result, 0));
  }

  private DeliveryOptions createDeliveryOptions(EventbusRpcRequest request) {
    DeliveryOptions deliveryOptions = new DeliveryOptions()
            .addHeader("x-request-id", request.id())
            .addHeader("x-request-policy", request.policy())
            .addHeader("x-request-address", request.address());
//    if (!Strings.isNullOrEmpty(request.action())) {
//      deliveryOptions.addHeader("action", request.action());
//    }
    Multimap<String, String> headers = request.headers();
    for (String key : headers.keySet()) {
      for (String value : headers.get(key)) {
        deliveryOptions.addHeader(key, value);
        if (key.equals("x-delivery-timeout") && StringUtils.isNumeric(value)) {
          deliveryOptions.setSendTimeout(Long.parseLong(value));
        }
      }
    }
    return deliveryOptions;
  }

  private JsonObject creeateMessage(EventbusRpcRequest request) {
    if (request.message() == null) {
      return new JsonObject();
    } else {
      return request.message();
    }
  }


  private void pointToPoint(JsonObject message, DeliveryOptions options,
                            Future<RpcResponse>
                                    completed) {
    vertx.eventBus().send(options.getHeaders().get("x-request-address"), message, options);
    JsonObject result = new JsonObject()
            .put("result", 1);

    String id = options.getHeaders().get("x-request-id");
    logSuccess("point-point", id, 0, result.encode().getBytes().length);
    completed.complete(
            RpcResponse.createJsonObject(options.getHeaders().get("x-request-id"), 200, result, 0));
  }

  private void reqResp(JsonObject message, DeliveryOptions options, Future<RpcResponse> completed) {
    long srated = System.currentTimeMillis();
    final String id = options.getHeaders().get("x-request-id");
    final String address = options.getHeaders().get("x-request-address");
    vertx.eventBus().<JsonObject>send(address, message, options, ar -> {
      long elapsedTime = System.currentTimeMillis() - srated;
      int bytes = 0;
      if (ar.succeeded()) {
        Object result = ar.result().body();
        if (result instanceof JsonObject) {
          bytes = result.toString().getBytes().length;
          logSuccess("req-resp", id, elapsedTime, bytes);
          completed.complete(
                  RpcResponse.createJsonObject(id, 200, (JsonObject) result, elapsedTime));
        } else if (result instanceof JsonArray) {
          bytes = result.toString().getBytes().length;
          logSuccess("req-resp", id, elapsedTime, bytes);
          completed.complete(
                  RpcResponse.createJsonArray(id, 200, (JsonArray) result, elapsedTime));
        } else {
          logError("req-resp", id, SystemException.create(DefaultErrorCode.INVALID_JSON));
          completed.fail(SystemException.create(DefaultErrorCode.INVALID_JSON));
          return;
        }
      } else {
        logError("req-resp", id, ar.cause());
        failed(completed, ar.cause());
      }
    });
  }

  private void logSuccess(String type, String id, long elapsedTime, int bytes) {
    Log.create(LOGGER)
            .setTraceId(id)
            .setLogType(LogType.CER)
            .setEvent("EVENTBUS." + type)
            .setMessage(" [{}ms] [{} bytes]")
            .addArg(elapsedTime)
            .addArg(bytes)
            .info();
  }

  private void logError(String type, String id, Throwable throwable) {
    Log.create(LOGGER)
            .setTraceId(id)
            .setLogType(LogType.CER)
            .setEvent("EVENTBUS." + type)
            .setThrowable(throwable)
            .error();
  }


  private void failed(Future<RpcResponse> completed,
                      Throwable throwable) {
    if (throwable instanceof ReplyException) {
      ReplyException ex = (ReplyException) throwable;
      if (ex.failureType() == ReplyFailure.NO_HANDLERS) {
        SystemException resourceNotFoundEx =
                SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
                        .set("details", "No handlers");
        completed.fail(resourceNotFoundEx);
      } else {
        ErrorCode errorCode = CustomErrorCode.create(ex.failureCode(), ex.getMessage(), 400);
        SystemException systemException
                = SystemException.create(errorCode);
        completed.fail(systemException);
      }
    } else {
      completed.fail(throwable);
    }
  }
}
