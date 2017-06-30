package com.edgar.direwolves.core.eventbus;

import com.google.common.collect.Lists;

import com.edgar.util.event.Event;
import com.edgar.util.event.EventAction;
import com.edgar.util.event.EventActionCodec;
import com.edgar.util.event.EventHead;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/6/30.
 *
 * @author Edgar  Date 2017/6/30
 */
public class EventbusUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventbusUtils.class);

  private static final List<EventActionCodec> codecList
          = Lists.newArrayList(ServiceLoader.load(EventActionCodec.class));

  public static void publish(Vertx vertx, Event event) {
    LOGGER.info("======> [{}] [PUB-SUB] [{}] [{}]",
                event.head().id(),
                toHeadString(event),
                toActionString(event));
    vertx.eventBus().publish(event.head().to(), createMessage(event),
                             createDeliveryOptions(event).addHeader("msg-type", "pub-sub"));
  }

  public static void send(Vertx vertx, Event event) {
    LOGGER.info("======> [{}] [POINT-POINT] [{}] [{}]",
                event.head().id(),
                event.head().action(),
                toHeadString(event),
                toActionString(event));
    vertx.eventBus().send(event.head().to(), createMessage(event),
                          createDeliveryOptions(event).addHeader("msg-type", "point-point"));
  }

  public static void request(Vertx vertx, Event event, Handler<AsyncResult<Event>>
          responseHandler) {
    LOGGER.info("======> [{}]  [REQUEST] [{}] [{}]",
                event.head().id(),
                event.head().action(),
                toHeadString(event),
                toActionString(event));
    vertx.eventBus()
            .<JsonObject>send(event.head().to(), createMessage(event),
                              createDeliveryOptions(event).addHeader("msg-type", "request"),
                              reply -> {
                                if (reply.failed()) {
                                  responseHandler.handle(Future.failedFuture(reply.cause()));
                                  return;
                                }
                                responseHandler
                                        .handle(Future.succeededFuture(null));
//                           responseHandler
//                                   .handle(Future.succeededFuture(reply.result().body()));
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

  private static JsonObject createMessage(Event event) {
    List<Map<String, Object>> actions = codecList.stream()
            .filter(c -> event.action().name().equalsIgnoreCase(c.name()))
            .map(c -> c.encode(event.action()))
            .collect(Collectors.toList());
    return new JsonObject(actions.get(0));
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

  public static void log(Message<JsonObject> message) {
    LOGGER.info("<====== [{}] [{}] [{}] [{}]",
                message.headers().get("id"),
                message.headers().get("msg-type"),
                toMessageHeaderString(message.headers()),
                toMessageString(message.body()));
  }

  public static String toMessageHeaderString(MultiMap multiMap) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry: multiMap) {
      sb.append(entry).append(';');
    }
    return sb.toString();
  }

  public static String toMessageString(JsonObject jsonObject) {
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
