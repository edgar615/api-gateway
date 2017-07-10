package com.edgar.direwolves.core.rpc.eventbus;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.direwolves.core.utils.Log;
import com.edgar.direwolves.core.utils.LogType;
import com.edgar.direwolves.core.utils.MultimapUtils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.eventbus.Event;
import com.edgar.util.vertx.eventbus.EventBuilder;
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
    Event event = createEvent(request);
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
      pubsub(event, deliveryOptions, future);
    } else if (EventbusEndpoint.POINT_POINT.equalsIgnoreCase(request.policy())) {
      pointToPoint(event, deliveryOptions, future);
    } else if (EventbusEndpoint.REQ_RESP.equalsIgnoreCase(request.policy())) {
      reqResp(event, deliveryOptions, future);
    } else {
      future.fail(SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE));
    }

    return future;
  }

  private void pubsub(Event event, DeliveryOptions options, Future<RpcResponse> completed) {

    vertx.eventBus().publish(event.address(), event, options);
    JsonObject result = new JsonObject()
            .put("result", 1);

    Log.create(LOGGER)
            .setTraceId(event.id())
            .setLogType(LogType.CER)
            .setEvent("EVENTBUS." + event.type())
            .setMessage(" [{}ms] [{} bytes]")
            .addArg(0)
            .addArg(result.encode().getBytes().length)
            .info();
    completed.complete(RpcResponse.createJsonObject(event.id(), 200, result, 0));
  }

  private DeliveryOptions createDeliveryOptions(EventbusRpcRequest request) {
    DeliveryOptions deliveryOptions = new DeliveryOptions()
            .addHeader("x-request-id", request.id());
    if (!Strings.isNullOrEmpty(request.action())) {
      deliveryOptions.addHeader("action", request.action());
    }
    return deliveryOptions;
  }

  private Event createEvent(EventbusRpcRequest request) {
    EventBuilder builder = Event.builder()
            .setId(request.id())
            .setAction(request.action())
            .setType(request.policy())
            .setAddress(request.address());
    Multimap<String, String> headers = request.headers();
    for (String key : headers.keySet()) {
      for (String value : headers.get(key)) {
        builder.addExt(key, value);
      }
    }
    if (request.message() == null) {
      builder.setBody(new JsonObject());
    } else {
      builder.setBody(request.message());
    }
    return builder.build();
  }


  private void pointToPoint(Event event, DeliveryOptions options, Future<RpcResponse> completed) {
    vertx.eventBus().send(event.address(), event, options);
    JsonObject result = new JsonObject()
            .put("result", 1);

    Log.create(LOGGER)
            .setTraceId(event.id())
            .setLogType(LogType.CER)
            .setEvent("EVENTBUS." + event.type())
            .setMessage(" [{}ms] [{} bytes]")
            .addArg(0)
            .addArg(result.encode().getBytes().length)
            .info();
    completed.complete(RpcResponse.createJsonObject(event.id(), 200, result, 0));
  }

  private void reqResp(Event event, DeliveryOptions options, Future<RpcResponse> completed) {
    long srated = System.currentTimeMillis();
    vertx.eventBus().<Event>send(event.address(), event, options, ar -> {
      long elapsedTime = System.currentTimeMillis() - srated;
      if (ar.succeeded()) {
        if (!(ar.result().body() instanceof Event)) {
          completed.fail(SystemException.create(DefaultErrorCode.INVALID_TYPE)
                                 .set("details", "expected:Event"));
          return;
        }
        Event response = ar.result().body();

        int bytes;
        if (ar.result() == null) {
          bytes = 0;
        } else {
          bytes = ar.result().toString().getBytes().length;
        }
        Log.create(LOGGER)
                .setTraceId(event.id())
                .setLogType(LogType.CER)
                .setEvent("EVENTBUS." + event.type())
                .setMessage(" [{}ms] [{} bytes]")
                .addArg(elapsedTime)
                .addArg(bytes)
                .info();
        if (ar.result().body() == null) {
          completed.fail(new NullPointerException("result is null"));
          return;
        }
        if (response.header().getBoolean("is_array", false)) {
          completed.complete(RpcResponse.createJsonArray(event.id(), 200,
                                                         response.body().getJsonArray("result",
                                                                                      new JsonArray()),
                                                         elapsedTime));
        } else {
          completed.complete(
                  RpcResponse.createJsonObject(event.id(), 200, response.body(), elapsedTime));
        }
      } else {
        Log.create(LOGGER)
                .setTraceId(event.id())
                .setLogType(LogType.CER)
                .setEvent("EVENTBUS")
                .setThrowable(ar.cause())
                .error();

        if (ar.cause() instanceof ReplyException) {
          ReplyException ex = (ReplyException) ar.cause();
          if (ex.failureType() == ReplyFailure.NO_HANDLERS) {
            SystemException resourceNotFoundEx =
                    SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE)
                            .set("details", "No handlers: " + event.address());
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
