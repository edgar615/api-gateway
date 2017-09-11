package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.plugin.appkey.discovery.AppKeyDiscovery;
import com.edgar.direwolves.plugin.appkey.discovery.JsonAppKeyImpoter;
import com.edgar.util.base.Randoms;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by edgar on 16-10-31.
 */
@RunWith(VertxUnitRunner.class)
public class JsonAppKeyImporterTest {

  private final List<Filter> filters = new ArrayList<>();

  String appKey = UUID.randomUUID().toString();

  String appKey2 = UUID.randomUUID().toString();

  String appSecret = UUID.randomUUID().toString();

  int appCode = Integer.parseInt(Randoms.randomNumber(3));

  String signMethod = "HMACMD5";

  AtomicInteger reqCount;

  AppKeyDiscovery discovery;

  private String namespace = UUID.randomUUID().toString();

  private Filter filter;

  private ApiContext apiContext;

  private String secretKey = UUID.randomUUID().toString();

  private String codeKey = UUID.randomUUID().toString();

  private Vertx vertx;

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
    discovery = AppKeyDiscovery.create(vertx, namespace);


    AtomicBoolean completed = new AtomicBoolean();
    JsonObject origin = new JsonObject()
            .put(secretKey, appSecret)
            .put(codeKey, appCode)
            .put("appKey", appKey);
    JsonObject config = new JsonObject()
            .put("data", new JsonArray().add(origin));
    Future<Void> future = Future.future();
    discovery.registerImporter(new JsonAppKeyImpoter(), config, future);
    future.setHandler(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        completed.set(true);
      }
    });
    Awaitility.await().until(() -> completed.get());
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close();
  }

  @Test
  public void testAppKey(TestContext testContext) {

    AtomicBoolean completed = new AtomicBoolean();
    discovery.getAppKey(appKey2, ar -> {
      testContext.assertNull(ar.result());
      completed.set(true);
    });

    Awaitility.await().until(() -> completed.get());

    AtomicBoolean completed2 = new AtomicBoolean();
    discovery.getAppKey(appKey, ar -> {
      testContext.assertNotNull(ar.result());
      completed2.set(true);
    });

    Awaitility.await().until(() -> completed2.get());

  }
}
