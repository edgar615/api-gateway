package com.edgar.direwolves.core.rpc.eventbus;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.direwolves.core.utils.Helper;
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
      pubsub(event,deliveryOptions,  future);
    } else if (EventbusEndpoint.POINT_POINT.equalsIgnoreCase(request.policy())) {
      pointToPoint(event,deliveryOptions, future);
    } else if (EventbusEndpoint.REQ_RESP.equalsIgnoreCase(request.policy())) {
      reqResp(event,deliveryOptions, future);
    } else {
      future.fail(SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE));
    }

    return future;
  }

  private void pubsub(Event event, DeliveryOptions options, Future<RpcResponse> completed) {

    vertx.eventBus().publish(event.address(), event, options);
    JsonObject result = new JsonObject()
            .put("result", 1);

    LOGGER.info("<------ [{}] [{}]",
                event.id(),
                event.type(),
                "OK",
                0,
                result.encode().getBytes().length
    );
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
    return builder.build();
  }


  private void pointToPoint(Event event, DeliveryOptions options, Future<RpcResponse> completed) {
    vertx.eventBus().send(event.address(), event, options);
    JsonObject result = new JsonObject()
            .put("result", 1);

    LOGGER.info("<------ [{}] [{}]",
                event.id(),
                event.type(),
                "OK",
                0,
                result.encode().getBytes().length
    );
    completed.complete(RpcResponse.createJsonObject(event.id(), 200, result, 0));
  }

  private void reqResp(Event event, DeliveryOptions options,Future<RpcResponse> completed) {
    long srated = System.currentTimeMillis();
    vertx.eventBus().<Event>send(event.address(), event, options, ar -> {
      long elapsedTime = System.currentTimeMillis() - srated;
      if (ar.succeeded()) {
        Event response = ar.result().body();

        int bytes;
        if (ar.result() == null) {
          bytes = 0;
        } else {
          bytes = ar.result().toString().getBytes().length;
        }

        LOGGER.info("<------ [{}] [{}] [{}] [{}ms] [{} bytes]",
                    event.id(),
                    response.type().toUpperCase(),
                    "OK",
                    elapsedTime,
                    bytes
        );
        if (ar.result().body() == null) {
          completed.fail(new NullPointerException("result is null"));
          return;
        }
        if (response.header().getBoolean("is_array", true)) {
          completed.complete(RpcResponse.createJsonArray(event.id(), 200,
                                                         response.body().getJsonArray("result",
                                                                                   new JsonArray()),
                                                         elapsedTime));
        } else {
          completed.complete(
                  RpcResponse.createJsonObject(event.id(), 200, response.body(), elapsedTime));
        }
      } else {
        Helper.logFailed(LOGGER, event.id(),
                         this.getClass().getSimpleName(),
                         ar.cause().getMessage());

        if (ar.cause() instanceof ReplyException) {
          ReplyException ex = (ReplyException) ar.cause();
          if (ex.failureType() == ReplyFailure.NO_HANDLERS) {
            SystemException resourceNotFoundEx =
                    SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                            .set("details", "No handlers for address " + event.address());
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
