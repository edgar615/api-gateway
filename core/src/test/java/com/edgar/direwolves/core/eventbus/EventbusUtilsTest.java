package com.edgar.direwolves.core.eventbus;

import com.edgar.util.event.Event;
import com.edgar.util.event.Message;
import com.edgar.util.event.Response;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2017/6/30.
 *
 * @author Edgar  Date 2017/6/30
 */
@RunWith(VertxUnitRunner.class)
public class EventbusUtilsTest {

  Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @Test
  public void testPublish() {
    String address = UUID.randomUUID().toString();
    JsonObject jsonObject = new JsonObject()
            .put("foo", "bar");

    AtomicInteger count = new AtomicInteger();
    vertx.eventBus().<JsonObject>consumer(address, message -> {
      EventbusUtils.log(message);
      JsonObject event = message.body();
      System.out.println(event);
      count.incrementAndGet();
    });
    vertx.eventBus().<JsonObject>consumer(address, message -> {
      System.out.println(EventbusUtils.toMessageHeaderString(message.headers()));
      System.out.println(EventbusUtils.toMessageString(message.body()));
      EventbusUtils.log(message);
      JsonObject event = message.body();
      System.out.println(event);
      count.incrementAndGet();
    });
    Event event = Event.create(address, Message.create("user.add", jsonObject.getMap()));
    EventbusUtils.publish(vertx, event);

    Awaitility.await().until(() -> count.get() == 2);
  }

  @Test
  public void testSend() {
    String address = UUID.randomUUID().toString();
    JsonObject jsonObject = new JsonObject()
            .put("foo", "bar");

    AtomicInteger count = new AtomicInteger();
    vertx.eventBus().<Event>consumer(address, ar -> {
      Event event = ar.body();
      System.out.println(event);
      count.incrementAndGet();
    });

    vertx.eventBus().<Event>consumer(address, ar -> {
      Event event = ar.body();
      System.out.println(event);
      count.incrementAndGet();
    });

    Event event = Event.create(address, Message.create("user.add", jsonObject.getMap()));
    EventbusUtils.send(vertx, event);

    Awaitility.await().until(() -> count.get() == 1);
  }

  @Test
  public void testRequest() {
    String address = UUID.randomUUID().toString();
    JsonObject jsonObject = new JsonObject()
            .put("foo", "bar");

    AtomicInteger count = new AtomicInteger();
    vertx.eventBus().<Event>consumer(address, ar -> {
      Event event = ar.body();
      System.out.println(event);
      String id = event.head().id() + "." + ar.replyAddress();
      Response response = Response.create(event.action().resource() + ".reply", 1, event.head()
              .id(), new JsonObject().put("bar", "foo").getMap());
      Event reply = Event.create(id, id, response);
      ar.reply(reply);
    });

    Event event = Event.create(address, Message.create("user.add", jsonObject.getMap()));
    EventbusUtils.request(vertx, event, ar -> {
      if (ar.succeeded()) {
        count.incrementAndGet();
        System.out.println(ar.result());
      } else {
        ar.cause().printStackTrace();
      }
    });

    Awaitility.await().until(() -> count.get() == 1);
  }
}
