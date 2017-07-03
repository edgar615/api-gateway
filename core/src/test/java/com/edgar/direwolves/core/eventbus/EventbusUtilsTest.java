package com.edgar.direwolves.core.eventbus;

import com.edgar.util.event.Event;
import com.edgar.util.event.Message;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.JsonUtils;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
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
    vertx.eventBus().registerCodec(new EventCodec());
  }

  @Test
  public void testPublish() {
    String address = UUID.randomUUID().toString();
    JsonObject jsonObject = new JsonObject()
            .put("foo", "bar");

    AtomicInteger count = new AtomicInteger();
    EventbusUtils.consumer(vertx, address, msg -> {
      System.out.println(msg.body());
      Assert.assertEquals("pub-sub", msg.body().head().ext("msg-type"));
      count.incrementAndGet();
    });
    EventbusUtils.consumer(vertx, address, msg -> {
      System.out.println(msg.body());
      Assert.assertEquals("pub-sub", msg.body().head().ext("msg-type"));
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
    EventbusUtils.consumer(vertx, address, msg -> {
      System.out.println(msg.body());
      Assert.assertEquals("point-point", msg.body().head().ext("msg-type"));
      count.incrementAndGet();
    });

    EventbusUtils.consumer(vertx, address, msg -> {
      System.out.println(msg);
      Assert.assertEquals("point-point", msg.body().head().ext("msg-type"));
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

    EventbusUtils.consumer(vertx, address, msg -> {
      System.out.println(msg.body());
      Assert.assertEquals("request", msg.body().head().ext("msg-type"));
      Message message = Message.create(msg.body().head().id(), msg.body().action().content());
      Event response = Event.create(msg.body().head().to(), message);
      EventbusUtils.reply(msg, Future.<Event>succeededFuture(response));
    });

    Event event = Event.create(address, Message.create("user.add", jsonObject.getMap()));
    AtomicInteger count = new AtomicInteger();
    EventbusUtils.request(vertx, event, ar -> {
      if (ar.succeeded()) {
        count.incrementAndGet();
        System.out.println(ar.result());
        Assert.assertEquals(event.head().id(), ar.result().action().resource());
      } else {
        ar.cause().printStackTrace();
      }
    });

    Awaitility.await().until(() -> count.get() == 1);
  }

  @Test
  public void testResponseError() {
    String address = UUID.randomUUID().toString();
    JsonObject jsonObject = new JsonObject()
        .put("foo", "bar");

    AtomicInteger count = new AtomicInteger();
    vertx.eventBus().<Event>consumer(address, msg -> {
      Event event = msg.body();
      System.out.println(event);
      EventbusUtils.reply(msg, Future.failedFuture(SystemException.create(DefaultErrorCode.INVALID_ARGS)));
    });

    Event event = Event.create(address, Message.create("user.add", jsonObject.getMap()));
    EventbusUtils.request(vertx, event, ar -> {
      if (ar.succeeded()) {
        Assert.fail();
        System.out.println(ar.result());
        Assert.assertEquals("reply.user.add", ar.result().action().resource());
      } else {
        System.out.println(ar.cause());
        ar.cause().printStackTrace();
        count.incrementAndGet();
      }
    });

    Awaitility.await().until(() -> count.get() == 1);
  }
}
