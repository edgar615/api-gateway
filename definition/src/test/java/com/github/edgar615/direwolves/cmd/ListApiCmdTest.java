package com.github.edgar615.direwolves.cmd;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.vertx.eventbus.Event;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class ListApiCmdTest extends BaseApiCmdTest {

  @Before
  public void setUp() {
    super.setUp();

    addMockApi();
  }

  @Test
  public void testMissNameShouldThrowValidationException(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.list")
            .setBody(new JsonObject())
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.list", event, ar -> {
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
  public void testListAll(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace);
    AtomicBoolean check = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.list")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.list", event, ar -> {
      if (ar.succeeded()) {
        JsonArray jsonArray = ar.result().body().body().getJsonArray("result");

        testContext.assertEquals(2, jsonArray.size());
        ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
        testContext.assertEquals("add_device", apiDefinition.name());
        apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(1));
        testContext.assertEquals("get_device", apiDefinition.name());
        check.set(true);
      } else {
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check.get());

  }

  @Test
  public void testListByFullname(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "get_device");

    AtomicBoolean check = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.list")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.list", event, ar -> {
      if (ar.succeeded()) {
        JsonArray jsonArray = ar.result().body().body().getJsonArray("result");

        testContext.assertEquals(1, jsonArray.size());
        ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
        testContext.assertEquals("get_device", apiDefinition.name());
        check.set(true);
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testListByWildcard(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "*device");

    AtomicBoolean check = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.list")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.list", event, ar -> {
      if (ar.succeeded()) {
        JsonArray jsonArray = ar.result().body().body().getJsonArray("result");

        testContext.assertEquals(2, jsonArray.size());
        ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
        testContext.assertEquals("add_device", apiDefinition.name());
        apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(1));
        testContext.assertEquals("get_device", apiDefinition.name());
        check.set(true);
      } else {
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testListByUndefinedWildcard(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "*rererere");

    AtomicBoolean check = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.list")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.list", event, ar -> {
      if (ar.succeeded()) {
        JsonArray jsonArray = ar.result().body().body().getJsonArray("result");

        testContext.assertEquals(0, jsonArray.size());
        check.set(true);
      } else {
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testListByStart(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("name", "*")
            .put("start", 1);

    AtomicBoolean check = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.list")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.list", event, ar -> {
      if (ar.succeeded()) {
        JsonArray jsonArray = ar.result().body().body().getJsonArray("result");

        testContext.assertEquals(1, jsonArray.size());
        ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
        testContext.assertEquals("get_device", apiDefinition.name());
        check.set(true);
      } else {
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testListByStartOut(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("start", 3);

    AtomicBoolean check = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.list")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.list", event, ar -> {
      if (ar.succeeded()) {
        JsonArray jsonArray = ar.result().body().body().getJsonArray("result");

        testContext.assertEquals(0, jsonArray.size());
        check.set(true);
      } else {
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testListByLimit(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("limit", 1);

    AtomicBoolean check = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.list")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.list", event, ar -> {
      if (ar.succeeded()) {
        JsonArray jsonArray = ar.result().body().body().getJsonArray("result");

        testContext.assertEquals(1, jsonArray.size());
        ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
        testContext.assertEquals("add_device", apiDefinition.name());
        check.set(true);
      } else {
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testListByLimitZero(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("limit", 0);

    AtomicBoolean check = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.list")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.list", event, ar -> {
      if (ar.succeeded()) {
        JsonArray jsonArray = ar.result().body().body().getJsonArray("result");

        testContext.assertEquals(0, jsonArray.size());
        check.set(true);
      } else {
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testListByStartAndLimit(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
        .put("start", 1)
        .put("limit", 1);

    AtomicBoolean check = new AtomicBoolean();
    Event event = Event.builder()
            .setAddress("direwolves.eb.api.list")
            .setBody(jsonObject)
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.list", event, ar -> {
      if (ar.succeeded()) {
        JsonArray jsonArray = ar.result().body().body().getJsonArray("result");

        testContext.assertEquals(1, jsonArray.size());
        ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonArray.getJsonObject(0));
        testContext.assertEquals("get_device", apiDefinition.name());
        check.set(true);
      } else {
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check.get());
  }

}
