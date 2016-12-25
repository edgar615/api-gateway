package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.cache.CacheProvider;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.direwolves.plugin.MockCacheProvider;
import com.edgar.util.base.EncryptUtils;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import com.edgar.util.vertx.task.Task;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by edgar on 16-10-31.
 */
@RunWith(VertxUnitRunner.class)
public class AppKeyAuthenticateTest {

  private final List<Filter> filters = new ArrayList<>();
  String appKey = UUID.randomUUID().toString();
  String appSecret = UUID.randomUUID().toString();
  String scope = "all";
  int appCode = Integer.parseInt(Randoms.randomNumber(3));
  private String namespace = UUID.randomUUID().toString();
  String signMethod = "HMACMD5";
  private Filter filter;
  CacheProvider cacheProvider = new MockCacheProvider();
  private ApiContext apiContext;

  private String secretKey = UUID.randomUUID().toString();

  private String codeKey = UUID.randomUUID().toString();

  private Vertx vertx;

  private String cacheAddress = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = Filter.create(AppKeyAuthenticate.class.getSimpleName(), vertx, new JsonObject()
        .put("app.secretKey", secretKey)
        .put("app.codeKey", codeKey)
        .put("service.cache.address", cacheAddress)
        .put("project.namespace", namespace));
    filters.clear();
    filters.add(filter);

    ProxyHelper.registerService(CacheProvider.class, vertx, cacheProvider, cacheAddress);

  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close();
  }

  @Test
  public void undefinedAppKeyShouldThrowInvalidReq(TestContext testContext) {

    createContext();

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> testContext.fail())
        .onFailure(t -> {
          testContext.assertTrue(t instanceof SystemException);
          SystemException ex = (SystemException) t;
          testContext.assertEquals(DefaultErrorCode.INVALID_REQ, ex.getErrorCode());
          async.complete();
        });
  }

  private void createContext() {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("appKey", appKey);
    params.put("nonce", Randoms.randomAlphabetAndNum(10));
    params.put("signMethod", signMethod);
    params.put("v", "1.0");
    params.put("sign", Randoms.randomAlphabetAndNum(16).toUpperCase());

    apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);

    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    definition.addPlugin(ApiPlugin.create(AppKeyCheckerPlugin.class.getSimpleName()));
  }

  @Test
  public void missParamShouldThrowValidationException(TestContext testContext) {

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, null, null);

    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    definition.addPlugin(ApiPlugin.create(AppKeyCheckerPlugin.class.getSimpleName()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> testContext.fail())
        .onFailure(t -> {
          testContext.assertTrue(t instanceof ValidationException);
          async.complete();
        });
  }

  @Test
  public void invalidSignShouldThrowInvalidReq(TestContext testContext) {

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("appKey", appKey);
    params.put("nonce", Randoms.randomAlphabetAndNum(10));
    params.put("signMethod", signMethod);
    params.put("v", "1.0");
    params.put("sign", Randoms.randomAlphabetAndNum(16).toUpperCase());

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    definition.addPlugin(ApiPlugin.create(AppKeyCheckerPlugin.class.getSimpleName()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> testContext.fail())
        .onFailure(e -> {
          testContext.assertTrue(e instanceof SystemException);
          SystemException ex = (SystemException) e;
          testContext.assertEquals(DefaultErrorCode.INVALID_REQ.getNumber(),
              ex.getErrorCode().getNumber());

          async.complete();
        });
  }

  @Test
  public void testSingWithoutBody(TestContext testContext) {

    cacheProvider.set(namespace + ":appKey:" + appKey, new JsonObject()
        .put(secretKey, appSecret)
        .put(codeKey, appCode), ar-> {

    });

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
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    definition.addPlugin(ApiPlugin.create(AppKeyCheckerPlugin.class.getSimpleName()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> {
          testContext.assertTrue(context.params().containsKey("sign"));
          async.complete();
        })
        .onFailure(t -> {
          t.printStackTrace();
          testContext.fail();
        });

  }

  @Test
  public void testSingWithBody(TestContext testContext) {

    cacheProvider.set(namespace + ":appKey:" + appKey, new JsonObject()
    .put(secretKey, appSecret)
    .put(codeKey, appCode), ar-> {

    });

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("appKey", appKey);
    params.put("nonce", Randoms.randomAlphabetAndNum(10));
    params.put("signMethod", signMethod);
    params.put("v", "1.0");
    params.put("deviceId", "1");

    JsonObject body = new JsonObject()
        .put("name", "$#$%$%$%")
        .put("code", 123434);

    params.put("body", body.encode());
    params.put("sign", signTopRequest(params, appSecret, signMethod));
    params.removeAll("body");

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, body);


    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    definition.addPlugin(ApiPlugin.create(AppKeyCheckerPlugin.class.getSimpleName()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> {
          testContext.assertTrue(context.params().containsKey("sign"));
          async.complete();
        })
        .onFailure(t -> {
          t.printStackTrace();
          testContext.fail();
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
