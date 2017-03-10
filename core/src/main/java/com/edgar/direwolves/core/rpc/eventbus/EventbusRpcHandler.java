package com.edgar.direwolves.core.rpc.eventbus;

import com.google.common.base.Strings;

import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.definition.PublishEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/12/30.
 *
 * @author Edgar  Date 2016/12/30
 */
public class EventbusRpcHandler implements RpcHandler {

  private final Vertx vertx;

  EventbusRpcHandler(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public String type() {
    return PublishEndpoint.TYPE;
  }

  @Override
  public Future<RpcResponse> handle(RpcRequest rpcRequest) {
    EventbusRpcRequest request = (EventbusRpcRequest) rpcRequest;
    DeliveryOptions deliveryOptions = new DeliveryOptions()
            .addHeader("id", request.id());
    if (!Strings.isNullOrEmpty(request.action())) {
      deliveryOptions.addHeader("action", request.action());
    }
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
    DeliveryOptions deliveryOptions = new DeliveryOptions()
            .addHeader("id", request.id());
    vertx.eventBus().publish(request.address(), request.message(), deliveryOptions);
    JsonObject result = new JsonObject()
            .put("result", 1);
    completed.complete(RpcResponse.createJsonObject(request.id(), 200, result, 0));
  }

  private void pointToPoint(EventbusRpcRequest request, Future<RpcResponse> completed) {
    DeliveryOptions deliveryOptions = new DeliveryOptions()
            .addHeader("id", request.id());
    vertx.eventBus().send(request.address(), request.message(), deliveryOptions);
    JsonObject result = new JsonObject()
            .put("result", 1);
    completed.complete(RpcResponse.createJsonObject(request.id(), 200, result, 0));
  }

  private void reqResp(EventbusRpcRequest request, Future<RpcResponse> completed) {
    DeliveryOptions deliveryOptions = new DeliveryOptions()
            .addHeader("id", request.id());
    if (!Strings.isNullOrEmpty(request.action())) {
      deliveryOptions.addHeader("action", request.action());
    }
    vertx.eventBus().<JsonObject>send(request.address(), request.message(), deliveryOptions, ar -> {
      if (ar.succeeded()) {
        if (!ar.result().body().containsKey("result")) {
          JsonObject result = ar.result().body();
          completed.complete(RpcResponse.createJsonObject(request.id(), 200, result, 0));
        } else {
          Object result = ar.result().body().getValue("result");
          if (result instanceof JsonArray) {
            completed.complete(
                    RpcResponse.createJsonArray(request.id(), 200, (JsonArray) result, 0));
          } else if (result instanceof JsonObject) {
            completed.complete(
                    RpcResponse.createJsonObject(request.id(), 200, (JsonObject) result, 0));
          } else {
            completed.complete(
                    RpcResponse.createJsonObject(request.id(), 200, ar.result().body(), 0));
          }

        }
      } else {
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
