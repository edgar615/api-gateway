package com.edgar.direwolves.plugin.appkey;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.base.EncryptUtils;
import com.edgar.util.base.Randoms;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by edgar on 16-10-31.
 */
@RunWith(VertxUnitRunner.class)
public class HttpAppKeyImporterTest {

  private final List<Filter> filters = new ArrayList<>();

  String appKey = UUID.randomUUID().toString();

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

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();

    filter = Filter.create(AppKeyFilter.class.getSimpleName(), vertx, new JsonObject()
            .put("app.secretKey", secretKey)
            .put("app.codeKey", codeKey)
            .put("namespace", namespace)
            .put("app.importer", new JsonObject()
                    .put("scan-period", 2000)
                    .put("url", "/appkey/import")));
    filters.clear();
    filters.add(filter);

    reqCount = new AtomicInteger(0);

    vertx.createHttpServer().requestHandler(req -> {
      if (reqCount.incrementAndGet() < 3) {
        JsonObject jsonObject = new JsonObject()
                .put("appKey", appKey)
                .put(secretKey, appSecret)
                .put(codeKey, appCode)
                .put("permissions", "all");
        JsonArray jsonArray = new JsonArray()
                .add(jsonObject);
        req.response().end(jsonArray.encode());
      } else {
        JsonObject jsonObject = new JsonObject()
                .put("appKey", UUID.randomUUID().toString())
                .put(secretKey, UUID.randomUUID().toString())
                .put(codeKey, appCode)
                .put("permissions", "all");
        JsonArray jsonArray = new JsonArray()
                .add(jsonObject);
        req.response().end(jsonArray.encode());
      }

    }).listen(9000, testContext.asyncAssertSuccess());

  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close();
  }

  @Test
  public void testSignWithoutBody(TestContext testContext) {

    try {
      TimeUnit.MILLISECONDS.sleep(2500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("appKey", appKey);
    params.put("nonce", Randoms.randomAlphabetAndNum(10));
    params.put("signMethod", signMethod);
    params.put("v", "1.0");
    params.put("deviceId", "1");

    params.put("sign", signTopRequest(params, appSecret, signMethod));
    params.removeAll("body");

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);


    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
            HttpEndpoint.http("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    definition.addPlugin(ApiPlugin.create(AppKeyPlugin.class.getSimpleName()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertTrue(context.params().containsKey("sign"));
              testContext.assertTrue(context.params().containsKey("signMethod"));
              testContext.assertTrue(context.params().containsKey("v"));
              testContext.assertTrue(context.params().containsKey("appKey"));
              async.complete();
            })
            .onFailure(t -> {
              t.printStackTrace();
              testContext.fail();
            });

    Awaitility.await().until(()-> reqCount.get() >= 3);
     task = Task.create();
    task.complete(apiContext);
    Async async2 = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();

            })
            .onFailure(t -> {
              async2.complete();
            });

  }

  private String getFirst(Multimap<String, String> params, String paramName) {
    return Lists.newArrayList(params.get(paramName)).get(0);
  }

  private String signTopRequest(Multimap<String, String> params, String secret, String signMethod) {
    // 第一步：检查参数是否已经排序
    String[] keys = params.keySet().toArray(new String[0]);
    Arrays.sort(keys);

    // 第二步：把所有参数名和参数值串在一起
    List<String> query = new ArrayList<>(params.size());
    for (String key : keys) {
      String value = getFirst(params, key);
      if (!Strings.isNullOrEmpty(value)) {
        query.add(key + "=" + value);
      }
    }
    String queryString = Joiner.on("&").join(query);
    String sign = null;
    try {
      if (EncryptUtils.HMACMD5.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacMd5(queryString, secret);
      } else if (EncryptUtils.HMACSHA256.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacSha256(queryString, secret);
      } else if (EncryptUtils.HMACSHA512.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacSha512(queryString, secret);
      } else if (EncryptUtils.MD5.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptMD5(secret + queryString + secret);
      }
    } catch (IOException e) {

    }
    return sign;
  }
}
