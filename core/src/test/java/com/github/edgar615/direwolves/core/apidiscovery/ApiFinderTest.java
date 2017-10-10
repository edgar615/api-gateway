package com.github.edgar615.direwolves.core.apidiscovery;

import com.google.common.collect.Lists;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
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
 * Created by Edgar on 2017/7/13.
 *
 * @author Edgar  Date 2017/7/13
 */
@RunWith(VertxUnitRunner.class)
public class ApiFinderTest {

  private Vertx vertx;

  private ApiDiscovery apiDiscovery;

  private ApiFinder apiFinder;

  private String namespace = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    JsonObject jsonObject = new JsonObject()
            .put("name", namespace);
    apiDiscovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions(jsonObject));
    apiFinder = new ApiFinderImpl(vertx, apiDiscovery);
  }

  @Test
  public void testPublish(TestContext testContext) {
    SimpleHttpEndpoint httpEndpoint = SimpleHttpEndpoint
            .http("get_device", HttpMethod.GET, "devices/",
                  80, "localhost");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    apiDiscovery.publish(apiDefinition, ar -> {
    });

    Awaitility.await().until(() -> apiFinder.size() == 1);

    AtomicBoolean check2 = new AtomicBoolean();
    apiFinder.getDefinitions("get", "/device", ar -> {
      testContext.assertEquals(1, ar.result().size());
      check2.set(true);
    });
    Awaitility.await().until(() -> check2.get());

    AtomicBoolean check3 = new AtomicBoolean();
    apiFinder.getDefinitions("get", "/devices", ar -> {
      testContext.assertEquals(0, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());
  }


  @Test
  public void testUnPublish(TestContext testContext) {
    SimpleHttpEndpoint httpEndpoint = SimpleHttpEndpoint
            .http("get_device", HttpMethod.GET, "devices/",
                  80, "localhost");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

//    AtomicBoolean check = new AtomicBoolean();
    apiDiscovery.publish(apiDefinition, ar -> {
//      check.set(true);
    });

    Awaitility.await().until(() -> apiFinder.size() == 1);

    apiDiscovery.unpublish("get_device", ar -> {

    });

    Awaitility.await().until(() -> apiFinder.size() == 0);
  }

}
