package com.edgar.direwolves.core.definition;

import com.google.common.collect.Lists;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2016/4/11.
 *
 * @author Edgar  Date 2016/4/11
 */
@RunWith(VertxUnitRunner.class)
public class ApiDiscoveryClusterTest {

  Vertx vertx;

  ApiDiscovery discovery;

  String namespace;

  @Before
  public void setUp() {
    namespace = UUID.randomUUID().toString();
    AtomicBoolean complete = new AtomicBoolean();
    Vertx.clusteredVertx(new VertxOptions().setClustered(true), ar -> {
      vertx = ar.result();
      complete.set(true);
    });
    Awaitility.await().until(() -> complete.get());
    discovery = ApiDiscovery.create(vertx, namespace);
  }

  @After
  public void clear() {
    discovery.close();
  }

  @Test
  public void testRegister(TestContext testContext) {
    HttpEndpoint httpEndpoint = HttpEndpoint
            .http("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    AtomicInteger seq = new AtomicInteger();
    List<ApiDefinition> definitions = new ArrayList<>();
    definitions.add(apiDefinition);

    ClusterDiscovery clusterDiscovery = new ClusterDiscovery(namespace, definitions, seq);
    Awaitility.await().until(() -> seq.get() == definitions.size());


    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(1, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    apiDefinition = ApiDefinition
            .create("get_device2", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    AtomicBoolean completed = new AtomicBoolean();
    clusterDiscovery.add(apiDefinition, completed);
    Awaitility.await().until(() -> completed.get());

    AtomicBoolean check2 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(2, ar.result().size());
      check2.set(true);
    });

    Awaitility.await().until(() -> check2.get());

  }

  @Test
  public void testUniqueName(TestContext testContext) {
    HttpEndpoint httpEndpoint = HttpEndpoint
            .http("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));

    AtomicInteger seq = new AtomicInteger();
    List<ApiDefinition> definitions = new ArrayList<>();
    definitions.add(apiDefinition);

    ClusterDiscovery clusterDiscovery = new ClusterDiscovery(namespace, definitions, seq);
    Awaitility.await().until(() -> seq.get() == definitions.size());


    AtomicBoolean check1 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(1, ar.result().size());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean completed = new AtomicBoolean();
    clusterDiscovery.add(apiDefinition, completed);
    Awaitility.await().until(() -> completed.get());

    AtomicBoolean check2 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(1, ar.result().size());
      check2.set(true);
    });

    Awaitility.await().until(() -> check2.get());
  }

  @Test
  public void testFilterByName(TestContext testContext) {
    HttpEndpoint httpEndpoint = HttpEndpoint
            .http("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinition apiDefinition = ApiDefinition
            .create("get_device", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));


    final List<ApiDefinition> definitions = new CopyOnWriteArrayList<>();
    definitions.add(apiDefinition);

    apiDefinition = ApiDefinition
            .create("get_device2", HttpMethod.GET, "device/", Lists.newArrayList(httpEndpoint));
    definitions.add(apiDefinition);

    AtomicInteger seq = new AtomicInteger();
    ClusterDiscovery clusterDiscovery = new ClusterDiscovery(namespace, definitions, seq);
    Awaitility.await().until(() -> seq.get() == definitions.size());

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
    discovery.getDefinitions(new JsonObject().put("name", "get_device"), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(1, ar.result().size());
      testContext.assertEquals("get_device", ar.result().get(0).name());
      check2.set(true);
    });
    Awaitility.await().until(() -> check2.get());

    AtomicBoolean check3 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject().put("name", "get_device3"), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(0, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());

    AtomicBoolean check4 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject().put("name", "get*"), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(2, ar.result().size());
      check4.set(true);
    });
    Awaitility.await().until(() -> check4.get());

    AtomicBoolean check5 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject().put("name", "*device*"), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(0, ar.result().size());
      check5.set(true);
    });
    Awaitility.await().until(() -> check5.get());

    AtomicBoolean check6 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject().put("name", "***"), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(0, ar.result().size());
      check6.set(true);
    });
    Awaitility.await().until(() -> check6.get());

    AtomicBoolean check7 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject().put("name", "*"), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(2, ar.result().size());
      check7.set(true);
    });
    Awaitility.await().until(() -> check7.get());

    AtomicBoolean check8 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject().put("name", "***"), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      testContext.assertEquals(0, ar.result().size());
      check8.set(true);
    });
    Awaitility.await().until(() -> check8.get());
  }

  private class ClusterDiscovery {
    private Vertx clusterVertx;

    private ApiDiscovery apiDiscovery;

    public ClusterDiscovery(String name, List<ApiDefinition> definitions, AtomicInteger seq) {
      Runnable runnable = () -> {
        Vertx.clusteredVertx(new VertxOptions().setClustered(true), ar -> {
          clusterVertx = ar.result();
          clusterVertx
                  .deployVerticle(new AbstractVerticle() {
                    @Override
                    public void start() throws Exception {
                      apiDiscovery = ApiDiscovery.create(vertx, name);
                      for (ApiDefinition definition : definitions) {
                        apiDiscovery.publish(definition,
                                             ar -> seq.incrementAndGet());
                      }
                    }
                  });
        });
      };

      new Thread(runnable).start();
    }

    public void add(ApiDefinition definition, AtomicBoolean complete) {
      apiDiscovery.publish(definition, ar -> complete.set(true));
    }

    public void close(AtomicBoolean complete) {
      clusterVertx.close(ar -> {
        complete.set(true);
      });
    }
  }
}
