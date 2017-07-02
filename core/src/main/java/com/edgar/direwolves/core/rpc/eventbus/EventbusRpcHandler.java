package com.edgar.direwolves.core.rpc.eventbus;

import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.eventbus.EventbusUtils;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.direwolves.core.utils.Helper;
import com.edgar.direwolves.core.utils.MultimapUtils;
import com.edgar.util.event.Event;
import com.edgar.util.event.Message;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.JsonUtils;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
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

    Event event = createEvent(request);
    Future<RpcResponse> future = Future.future();
    if (EventbusEndpoint.PUB_SUB.equalsIgnoreCase(request.policy())) {
      pubsub(event, future);
    } else if (EventbusEndpoint.POINT_POINT.equalsIgnoreCase(request.policy())) {
      pointToPoint(event, future);
    } else if (EventbusEndpoint.REQ_RESP.equalsIgnoreCase(request.policy())) {
      reqResp(event, future);
    } else {
      future.fail(SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE));
    }

    return future;
  }

  private void pubsub(Event event, Future<RpcResponse> completed) {
    EventbusUtils.publish(vertx, event);

    JsonObject result = new JsonObject()
        .put("result", 1);

    LOGGER.info("<------ [{}] [{}] [{}] [{}ms] [{} bytes]",
        event.head().id(),
        "pub-sub",
        "OK",
        0,
        result.encode().getBytes().length
    );
    completed.complete(RpcResponse.createJsonObject(event.head().id(), 200, result, 0));
  }

  private Event createEvent(EventbusRpcRequest request) {
    Event event = Event.create(request.id(), request.address(), Message.create(request.name(), JsonUtils.toMap(request.message())));
    Multimap<String, String> headers = request.headers();
    for (String key : headers.keySet()) {
      for (String value : headers.get(key)) {
        event.head().addExt(key, value);
      }
    }
    return event;
  }

  private void pointToPoint(Event event, Future<RpcResponse> completed) {
    EventbusUtils.send(vertx, event);
    JsonObject result = new JsonObject()
        .put("result", 1);

    LOGGER.info("<------ [{}] [{}] [{}] [{}ms] [{} bytes]",
        event.head().id(),
        "point-point",
        "OK",
        0,
        result.encode().getBytes().length
    );
    completed.complete(RpcResponse.createJsonObject(event.head().id(), 200, result, 0));
  }

  private void reqResp(Event event, Future<RpcResponse> completed) {
    long srated = System.currentTimeMillis();
    EventbusUtils.request(vertx, event, ar -> {
      long elapsedTime = System.currentTimeMillis() - srated;
      if (ar.succeeded()) {
        Event replyEvent = ar.result();
        int bytes;
        if (ar.result() == null) {
          bytes = 0;
        } else {
          bytes = ar.result().toString().getBytes().length;
        }
        LOGGER.info("<------ [{}] [{}] [{}] [{}ms] [{} bytes]",
            event.head().id(),
            "request",
            "OK",
            elapsedTime,
            bytes);
        completed.complete(
            RpcResponse
                .createJsonObject(replyEvent.head().id(), 200,
                    new JsonObject(((Message) replyEvent.action()).content()),
                    elapsedTime));
      } else {
        Helper.logFailed(LOGGER, event.head().id(),
            this.getClass().getSimpleName(),
            ar.cause().getMessage());
        completed.fail(ar.cause());
      }
    });
  }
}
