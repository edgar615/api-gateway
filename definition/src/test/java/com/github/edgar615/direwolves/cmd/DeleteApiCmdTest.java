package com.github.edgar615.direwolves.cmd;

import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.vertx.eventbus.Event;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class DeleteApiCmdTest extends BaseApiCmdTest {

  @Before
  public void setUp() {
   super.setUp();
    addMockApi();
  }

  @Test
  public void testMissNameShouldThrowValidationException(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.delete")
            .setBody(new JsonObject())
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.delete", event, ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        ar.cause().printStackTrace();
        testContext.assertTrue(ar.cause() instanceof ReplyException);
        testContext.assertEquals(DefaultErrorCode.INVALID_ARGS.getNumber(),
                                 ReplyException.class.cast(ar.cause()).failureCode());
        check.set(true);
      }
    });
    Awaitility.await().until(() -> check.get());

  }

  @Test
  public void testDeleteApiByFullname(TestContext testContext) {
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(2, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check2 = new AtomicBoolean();
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "add_device");

    Event event = Event.builder()
            .setAddress("direwolves.eb.api.delete")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.delete", event, ar -> {
      if (ar.succeeded()) {
        check2.set(true);
      } else {
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
  public void testDeleteApiByUndefinedNameShouldSuccess(TestContext testContext) {
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(2, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check2 = new AtomicBoolean();
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", UUID.randomUUID().toString());

    Event event = Event.builder()
            .setAddress("direwolves.eb.api.delete")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.delete", event, ar -> {
      if (ar.succeeded()) {
        check2.set(true);
      } else {
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
      testContext.assertEquals(2, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());
  }

  @Test
  public void testDeleteApiByWildcard(TestContext testContext) {
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(2, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check2 = new AtomicBoolean();
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "*device");

    Event event = Event.builder()
            .setAddress("direwolves.eb.api.delete")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.delete", event, ar -> {
      if (ar.succeeded()) {
        check2.set(true);
      } else {
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
      testContext.assertEquals(0, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());
  }

}
