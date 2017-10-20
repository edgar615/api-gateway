package com.github.edgar615.direwolves.cmd;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.util.exception.DefaultErrorCode;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class AddApiCmdTest extends BaseApiCmdTest {

  @Test
  public void testInvalidArg(TestContext testContext) {
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(0, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device")
            .put("method", "POST")
            .put("path", "/devices");
    JsonArray endpoints = new JsonArray()
            .add(new JsonObject().put("type", "simple-http").put("host", "localhost")
                         .put("port", 80)
                         .put("name", "add_device")
                         .put("service", "device")
                         .put("method", "POST")
                         .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);

    AtomicBoolean check2 = new AtomicBoolean();
    JsonObject event = new JsonObject().put("data", jsonObject.encode());
    vertx.eventBus().<JsonObject>send("direwolves.eb.api.add", event, ar -> {
      if (ar.succeeded()) {
        System.out.println(ar.result().body());
//        testContext.assertEquals(1, ar.result().getInteger("result"));
        testContext.fail();
      } else {
        ar.cause().printStackTrace();
        testContext.assertTrue(ar.cause() instanceof ReplyException);
        testContext.assertEquals(DefaultErrorCode.INVALID_ARGS.getNumber(),
                                 ReplyException.class.cast(ar.cause()).failureCode());
        check2.set(true);
      }
    });
    Awaitility.await().until(() -> check2.get());

    AtomicBoolean check3 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      System.out.println(ar.result());
      testContext.assertEquals(0, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());
  }

  @Test
  public void testAddApiSuccess(TestContext testContext) {

    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(0, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device")
            .put("method", "POST")
            .put("path", "/devices");
    JsonArray endpoints = new JsonArray()
            .add(new JsonObject().put("type", "simple-http").put("host", "localhost")
                         .put("port", 80)
                         .put("name", "add_device")
                         .put("service", "device")
                         .put("method", "POST")
                         .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);

    AtomicBoolean check2 = new AtomicBoolean();
    JsonObject event = new JsonObject()
            .put("namespace", namespace).put("data", jsonObject.encode());
    vertx.eventBus().<JsonObject>send("direwolves.eb.api.add", event, ar -> {
      if (ar.succeeded()) {
        System.out.println(ar.result().body());
//        testContext.assertEquals(1, ar.result().getInteger("result"));
        check2.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check2.get());

    AtomicBoolean check3 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      System.out.println(ar.result());
      testContext.assertEquals(1, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());

  }

  @Test
  public void testAddApiWithPlugin(TestContext testContext) {
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(0, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device")
            .put("method", "POST")
            .put("path", "/devices");
    JsonArray endpoints = new JsonArray()
            .add(new JsonObject().put("type", "simple-http").put("host", "localhost")
                         .put("port", 80)
                         .put("name", "add_device")
                         .put("service", "device")
                         .put("method", "POST")
                         .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);
    jsonObject.put("authentication", true);

    AtomicBoolean check2 = new AtomicBoolean();
    JsonObject event = new JsonObject().put("namespace", namespace)
            .put("data", jsonObject.encode());
    vertx.eventBus().<JsonObject>send("direwolves.eb.api.add", event, ar -> {
      if (ar.succeeded()) {
        System.out.println(ar.result().body());
//        testContext.assertEquals(1, ar.result().getInteger("result"));
        check2.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check2.get());

    AtomicBoolean check3 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      System.out.println(ar.result());
      testContext.assertEquals(1, ar.result().size());
      ApiDefinition definition = ar.result().get(0);
      testContext.assertEquals(1, definition.plugins().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());


  }

}
