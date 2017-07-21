package com.edgar.direwolves.plugin.appkey;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.direwolves.plugin.appkey.discovery.AppKeyDiscovery;
import com.edgar.direwolves.plugin.appkey.discovery.HttpAppKeyImporter;
import com.edgar.util.base.EncryptUtils;
import com.edgar.util.base.Randoms;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by edgar on 16-10-31.
 */
@RunWith(VertxUnitRunner.class)
public class HttpAppKeyImporterTest {

  private final List<Filter> filters = new ArrayList<>();

  String appKey = UUID.randomUUID().toString();

  String appKey2 = UUID.randomUUID().toString();

  String appSecret = UUID.randomUUID().toString();

  int appCode = Integer.parseInt(Randoms.randomNumber(3));

  String signMethod = "HMACMD5";

  private String namespace = UUID.randomUUID().toString();

  private Filter filter;

  private ApiContext apiContext;

  private String secretKey = UUID.randomUUID().toString();

  private String codeKey = UUID.randomUUID().toString();

  private Vertx vertx;

  AtomicInteger reqCount;

  AppKeyDiscovery discovery;

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
    reqCount = new AtomicInteger(0);
    discovery = AppKeyDiscovery.create(vertx, namespace);


    AtomicBoolean completed = new AtomicBoolean();
    int port = Integer.parseInt(Randoms.randomNumber(4));
    JsonObject config = new JsonObject()
            .put("port", port)
            .put("scan-period", 1500);
    Future<Void> future = Future.future();
    discovery.registerImporter(new HttpAppKeyImporter(), config, future);
    future.setHandler(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        completed.set(true);
      }
    });

    vertx.createHttpServer().requestHandler(req -> {
      if (reqCount.incrementAndGet() < 3) {
        JsonObject jsonObject = new JsonObject()
                .put("appKey", appKey)
                .put(secretKey, appSecret)
                .put(codeKey, appCode)
                .put("permissions", "all");
        JsonObject jsonObject2 = new JsonObject()
                .put("appKey", appKey2)
                .put(secretKey, appSecret)
                .put(codeKey, appCode)
                .put("permissions", "all");
        JsonArray jsonArray = new JsonArray()
                .add(jsonObject).add(jsonObject2);
        req.response().end(jsonArray.encode());
      } else {
        JsonObject jsonObject = new JsonObject()
                .put("appKey", UUID.randomUUID().toString())
                .put(secretKey, UUID.randomUUID().toString())
                .put(codeKey, appCode)
                .put("permissions", "all");
        JsonObject jsonObject2 = new JsonObject()
                .put("appKey", appKey2)
                .put(secretKey, appSecret)
                .put(codeKey, appCode)
                .put("permissions", "all");
        JsonArray jsonArray = new JsonArray()
                .add(jsonObject).add(jsonObject2);
        req.response().end(jsonArray.encode());
      }

    }).listen(port, testContext.asyncAssertSuccess());

  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close();
  }

  @Test
  public void testAppKey(TestContext testContext) {
    Awaitility.await().until(()-> reqCount.get() >= 5);

    AtomicBoolean completed = new AtomicBoolean();
    discovery.getAppKey(appKey, ar -> {
      testContext.assertNull(ar.result());
      completed.set(true);
    });

    Awaitility.await().until(() -> completed.get());

    AtomicBoolean completed2 = new AtomicBoolean();
    discovery.getAppKey(appKey2, ar -> {
      testContext.assertNotNull(ar.result());
      completed2.set(true);
    });

    Awaitility.await().until(() -> completed2.get());

  }
}
