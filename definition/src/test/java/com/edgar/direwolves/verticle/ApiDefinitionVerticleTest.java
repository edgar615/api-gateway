package com.edgar.direwolves.verticle;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.apidiscovery.ApiDiscoveryOptions;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.apidiscovery.ApiDiscovery;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.definition.SimpleHttpEndpoint;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.vertx.eventbus.Event;
import com.edgar.util.vertx.eventbus.EventCodec;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2016/4/11.
 *
 * @author Edgar  Date 2016/4/11
 */
@RunWith(VertxUnitRunner.class)
public class ApiDefinitionVerticleTest {

  ApiDiscovery discovery;

  String namespace;

  Vertx vertx;

  @Before
  public void setUp(TestContext testContext) {
    namespace = UUID.randomUUID().toString();
    vertx = Vertx.vertx();
    vertx.eventBus().registerDefaultCodec(Event.class, new EventCodec());
    vertx.deployVerticle(ApiDefinitionVerticle.class.getName(), testContext.asyncAssertSuccess());

    discovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName(namespace));
    SimpleHttpEndpoint httpEndpoint = SimpleHttpEndpoint
            .http("get_device", HttpMethod.GET, "devices/",
                  80, "localhost");
    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.publish(apiDefinition, ar -> {
      if (ar.succeeded()) {
        check1.set(true);
      } else {
        ar.cause().printStackTrace();
      }
    });
    Awaitility.await().until(() -> check1.get());

    apiDefinition = ApiDefinition
            .create("get_device2", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));
    AtomicBoolean check2 = new AtomicBoolean();
    discovery.publish(apiDefinition, ar -> {
      if (ar.succeeded()) {
        check2.set(true);
      } else {
        ar.cause().printStackTrace();
      }
    });
    Awaitility.await().until(() -> check2.get());
  }

  @Test
  public void testGetApiEventbus(TestContext testContext) {
    Async async = testContext.async();
    Event event = Event.builder()
            .setBody(new JsonObject().put("name", "get_device").put("namespace", namespace))
            .build();
    vertx.eventBus().<Event>send("direwolves.eb.api.get",
        event, ar -> {
          if (ar.succeeded()) {
            System.out.println(ar.result().body());
            async.complete();
          } else {
            ar.cause().printStackTrace();
            testContext.fail();
          }
        });
  }

  @Test
  public void testGetUndefinedApiEventbus(TestContext testContext) {
    Async async = testContext.async();
    Event event = Event.builder()
            .setBody(new JsonObject().put("name", Randoms.randomAlphabet(10)).put("namespace", namespace))
            .build();
    vertx.eventBus().<JsonObject>send("direwolves.eb.api.get",
        event, ar-> {
          if (ar.succeeded()) {
            testContext.fail();
          } else {
            testContext.assertTrue(ar.cause() instanceof ReplyException);
            ReplyException ex = (ReplyException) ar.cause();
            testContext.assertEquals(DefaultErrorCode.RESOURCE_NOT_FOUND.getNumber(), ex.failureCode());
            async.complete();
          }
        });
  }

}
