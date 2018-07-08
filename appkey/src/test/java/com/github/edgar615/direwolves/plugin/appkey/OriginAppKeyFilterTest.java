package com.github.edgar615.direwolves.plugin.appkey;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.ValidationException;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by edgar on 16-10-31.
 */
@RunWith(VertxUnitRunner.class)
public class OriginAppKeyFilterTest extends AbstractAppKeyFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  String appKey = UUID.randomUUID().toString();

  String appSecret = UUID.randomUUID().toString();

  int clientCode = Integer.parseInt(Randoms.randomNumber(3));

  String signMethod = "HMACMD5";

  private Filter filter;

  private ApiContext apiContext;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    filters.clear();

  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close();
  }

  @Test
  public void undefinedAppKeyShouldThrowInvalidReq(TestContext testContext) {
    JsonObject origin = new JsonObject()
            .put("appSecret", appSecret)
            .put("clientCode", clientCode)
            .put("appKey", UUID.randomUUID().toString());
    JsonObject config = new JsonObject()
            .put("data", new JsonArray().add(origin));
    filter = Filter.create(AppKeyFilter.class.getSimpleName(), vertx, new JsonObject()
            .put("appkey", config));
    filters.add(filter);
    apiContext = createContext(appKey, signMethod);

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

  @Test
  public void missParamShouldThrowValidationException(TestContext testContext) {

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, null, null);

    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                    80, "localhost");
    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    definition.addPlugin(ApiPlugin.create(AppKeyPlugin.class.getSimpleName()));

    JsonObject origin = new JsonObject()
            .put("appSecret", appSecret)
            .put("clientCode", clientCode)
            .put("appKey", appKey);
    JsonObject config = new JsonObject()
            .put("data", new JsonArray().add(origin));
    filter = Filter.create(AppKeyFilter.class.getSimpleName(), vertx, new JsonObject()
            .put("appkey", config));
    filters.add(filter);
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
    JsonObject origin = new JsonObject()
            .put("appSecret", appSecret)
            .put("clientCode", clientCode)
            .put("appKey", appKey);
    JsonObject config = new JsonObject()
            .put("data", new JsonArray().add(origin));

    filter = Filter.create(AppKeyFilter.class.getSimpleName(), vertx, new JsonObject()
            .put("appkey", config));
    filters.add(filter);

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("appKey", appKey);
    params.put("nonce", Randoms.randomAlphabetAndNum(10));
    params.put("signMethod", signMethod);
    params.put("v", "1.0");
    params.put("sign", Randoms.randomAlphabetAndNum(16).toUpperCase());

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                    80, "localhost");
    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    definition.addPlugin(ApiPlugin.create(AppKeyPlugin.class.getSimpleName()));

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
  public void testSignWithoutBody(TestContext testContext) {

    JsonObject origin = new JsonObject()
            .put("appSecret", appSecret)
            .put("clientCode", clientCode)
            .put("appKey", appKey);
    JsonObject config = new JsonObject()
            .put("data", new JsonArray().add(origin));

    filters.clear();
    filter = Filter.create(AppKeyFilter.class.getSimpleName(), vertx, new JsonObject()
            .put("appkey", config));
    filters.add(filter);

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("appKey", appKey);
    params.put("nonce", Randoms.randomAlphabetAndNum(10));
    params.put("signMethod", signMethod);
    params.put("v", "1.0");
    params.put("deviceId", "1");

    params.put("sign", signTopRequest(params, appSecret, signMethod));
    params.removeAll("body");

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);


    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                    80, "localhost");
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

  }

  @Test
  public void testSignWithBody(TestContext testContext) {
    JsonObject origin = new JsonObject()
            .put("appSecret", appSecret)
            .put("clientCode", clientCode)
            .put("appKey", appKey);
    JsonObject config = new JsonObject()
            .put("data", new JsonArray().add(origin));

    filters.clear();
    filter = Filter.create(AppKeyFilter.class.getSimpleName(), vertx, new JsonObject()
            .put("appkey", config));
    filters.add(filter);

    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

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

    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                    80, "localhost");
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

  }


}