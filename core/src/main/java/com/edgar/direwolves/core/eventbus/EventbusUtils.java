package com.edgar.direwolves.core.eventbus;

import com.edgar.util.event.Event;
import com.edgar.util.event.EventAction;
import com.edgar.util.event.EventHead;
import com.edgar.util.exception.SystemException;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/6/30.
 *
 * @author Edgar  Date 2017/6/30
 */
public class EventbusUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventbusUtils.class);

  public static void publish(Vertx vertx, Event event) {
    publish(vertx, event, new DeliveryOptions());
  }

  public static void publish(Vertx vertx, Event event, DeliveryOptions options) {
    Objects.requireNonNull(event);
    Objects.requireNonNull(options);
    options.setCodecName(EventCodec.class.getName());
    options.addHeader("x-request-id", event.head().id());
    event.head().addExt("msg-type", "pub-sub");
    vertx.eventBus().publish(event.head().to(), event, options);
    outLog(event, "PUB-SUB");
  }

  public static void send(Vertx vertx, Event event) {
    send(vertx, event, new DeliveryOptions());
  }

  public static void send(Vertx vertx, Event event, DeliveryOptions options) {
    Objects.requireNonNull(event);
    Objects.requireNonNull(options);
    options.setCodecName(EventCodec.class.getName());
    options.addHeader("x-request-id", event.head().id());
    event.head().addExt("msg-type", "point-point");
    vertx.eventBus().send(event.head().to(), event, options);
    outLog(event, "POINT-POINT");
  }

  public static void request(Vertx vertx, Event event,
                             Handler<AsyncResult<Event>> responseHandler) {
    request(vertx, event, new DeliveryOptions(), responseHandler);
  }

  public static void request(Vertx vertx, Event event, DeliveryOptions options,
                             Handler<AsyncResult<Event>> responseHandler) {
    Objects.requireNonNull(event);
    Objects.requireNonNull(options);
    options.setCodecName(EventCodec.class.getName());
    options.addHeader("x-request-id", event.head().id());
    event.head().addExt("msg-type", "request");
    vertx.eventBus()
        .<Event>send(event.head().to(), event,
            options,
            reply -> {
              if (reply.failed()) {
                responseHandler.handle(Future.failedFuture(FailureTransformer.create().apply(reply.cause())));
                return;
              }
              inLog(reply.result().body());
              responseHandler.handle(Future.succeededFuture(reply.result().body()));
            });
  }

  public static void reply(Message<Event> message, Future<Event> completeFuture) {
    reply(message, completeFuture, new DeliveryOptions());

  }

  public static void reply(Message<Event> message, Future<Event> completeFuture,
                           DeliveryOptions options) {
    Objects.requireNonNull(options);
    options.setCodecName(EventCodec.class.getName());
    Event request = message.body();
    completeFuture.setHandler(ar -> {
      if (ar.succeeded()) {
        Event response = ar.result();
        response.head().addExt("msg-type", "response");
        response.head().addExt("reply", request.head().id());
        outLog(response, "RESPONSE");
        message.reply(response, options);
      } else {
        SystemException se = FailureTransformer.create().apply(ar.cause());
        message.fail(se.getErrorCode().getNumber(), se.getMessage());
      }
    });

  }

  public static void consumer(Vertx vertx, String address, Handler<Message<Event>> messageHandler) {
    vertx.eventBus().<Event>consumer(address, message -> {
      try {
        inLog(message.body());
        messageHandler.handle(message);
      } catch (Exception e) {
        LOGGER.error("<====== [event.handled.failed] [{}]",
            message.body(), e);
      }
    });
  }

  private static String toHeadString(Event event) {
    EventHead head = event.head();
    StringBuilder s = new StringBuilder();
    s.append("id:").append(head.id()).append(";")
        .append("to:").append(head.to()).append(";")
        .append("action:").append(head.action()).append(";")
        .append("timestamp:").append(head.timestamp()).append(";")
        .append("duration:").append(head.duration()).append(";");
    head.ext().forEach((k, v) -> s.append(k).append(":").append(v).append(";"));

    return s.toString();
  }

  private static String toActionString(Event event) {
    EventAction action = event.action();
    List<String> actions = Event.codecList.stream()
        .filter(c -> action.name().equalsIgnoreCase(c.name()))
        .map(c -> c.encode(action))
        .map(m -> {
          StringBuilder s = new StringBuilder();
          m.forEach((k, v) -> s.append(k + ":" + v + ";"));
          return s.toString();
        })
        .collect(Collectors.toList());
    return actions.get(0);
  }

  private static DeliveryOptions createDeliveryOptions(Event event) {
    DeliveryOptions deliveryOptions = new DeliveryOptions();
    deliveryOptions.addHeader("id", event.head().id());
    deliveryOptions.addHeader("to", event.head().to());
    deliveryOptions.addHeader("action", event.head().action());
    deliveryOptions.addHeader("timestamp", event.head().timestamp() + "");
    deliveryOptions.addHeader("duration", event.head().duration() + "");
    event.head().ext().forEach((k, v) -> deliveryOptions.addHeader(k, v));
    return deliveryOptions;
  }

  private static void outLog(Event event, String type) {
    LOGGER.info("======> [{}] [{}] [{}] [{}]",
        type,
        event.head().id(),
        toHeadString(event),
        toActionString(event));
  }

  private static void inLog(Event event) {
    LOGGER.info("<====== [{}] [{}] [{}]",
        event.head().id(),
        toHeadString(event),
        toActionString(event));
  }

  private static String toMessageHeaderString(MultiMap multiMap) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : multiMap) {
      sb.append(entry).append(';');
    }
    return sb.toString();
  }

  private static String toMessageString(JsonObject jsonObject) {
    StringBuilder sb = new StringBuilder();
    for (String field : jsonObject.fieldNames()) {
      sb.append(field)
          .append(":")
          .append(jsonObject.getValue(field))
          .append(";");
    }
    return sb.toString();
  }
}
