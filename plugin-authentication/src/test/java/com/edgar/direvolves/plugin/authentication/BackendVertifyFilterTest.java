package com.edgar.direvolves.plugin.authentication;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.base.EncryptUtils;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class BackendVertifyFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Filter filter;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = Filter.create(BackendVertifyFilter.class.getSimpleName(), vertx,
                           new JsonObject()
                                   .put("backend.permitted", new JsonArray().add("987654321")));

    filters.clear();
    filters.add(filter);

  }

  @Test
  public void missUsernameShouldThrowValidationException(TestContext testContext) {
    ApiContext apiContext = createContext(null, null, null);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            })
            .onFailure(throwable -> {
              testContext.assertTrue(throwable instanceof ValidationException);
              ValidationException ex = (ValidationException) throwable;
              testContext.assertTrue(ex.getErrorDetail().containsKey("username"));
              testContext.assertTrue(ex.getErrorDetail().containsKey("code"));
              testContext.assertTrue(ex.getErrorDetail().containsKey("sign"));
              async.complete();
            });
  }

  @Test
  public void notAllowedUsernameShouldThrow1004(TestContext testContext) {
    ApiContext apiContext = createContext("145687", "123456", "dree");

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            })
            .onFailure(throwable -> {
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.NO_AUTHORITY, ex.getErrorCode());
              async.complete();
            });

  }

  @Test
  public void InvalidSignUsernameShouldThrow1003(TestContext testContext) {
    ApiContext apiContext = createContext("987654321", "123456", "dree");

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.UNKOWN_ACCOUNT, ex.getErrorCode());
              async.complete();
            });

  }

  @Test
  public void ExpireSignUsernameShouldThrow1023(TestContext testContext) throws IOException {
    long exp =  Instant.now().getEpochSecond() - 10 * 60;
    JsonObject jsonObject = new JsonObject()
            .put("exp",exp);
    String chaim = Base64.getUrlEncoder().encodeToString(jsonObject.encode().getBytes());
    String code = Randoms.randomNumber(6);
    String sign = EncryptUtils.encryptHmacMd5(chaim, code + exp);
    ApiContext apiContext = createContext("987654321", code, chaim + "." +sign);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.EXPIRE, ex.getErrorCode());
              async.complete();
            });

  }

  @Test
  public void InvalidSignUsernameShouldThrow1001(TestContext testContext) throws IOException {
    long exp =  Instant.now().getEpochSecond() - 3 * 60;
    JsonObject jsonObject = new JsonObject()
            .put("exp",exp);
    String chaim = Base64.getUrlEncoder().encodeToString(jsonObject.encode().getBytes());
    String code = Randoms.randomNumber(6);
    ApiContext apiContext = createContext("987654321", code, chaim + "." +Randoms
            .randomAlphabetAndNum(20));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.UNKOWN_ACCOUNT, ex.getErrorCode());
              async.complete();
            });

  }

  @Test
  public void validArgShouldSuccess(TestContext testContext) throws IOException {
    long exp =  Instant.now().getEpochSecond() - 3 * 60;
    JsonObject jsonObject = new JsonObject()
            .put("exp",exp);
    String chaim = Base64.getUrlEncoder().encodeToString(jsonObject.encode().getBytes());
    String code = Randoms.randomNumber(6);
    String sign = EncryptUtils.encryptHmacMd5(chaim, code + exp);
    ApiContext apiContext = createContext("987654321", code, chaim + "." +sign);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              async.complete();
            })
            .onFailure(throwable -> {
              testContext.fail();
            });

  }

  private ApiContext createContext(String tel, String code, String sign) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");
    JsonObject body = new JsonObject()
            .put("username", tel)
            .put("code", code)
            .put("sign", sign);
    ApiContext apiContext =
            ApiContext.create(HttpMethod.POST, "/devices", headers, params, body);
    HttpEndpoint httpEndpoint =
            HttpEndpoint.http("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists
            .newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    BackendVertifyPlugin plugin = (BackendVertifyPlugin) ApiPlugin.create(BackendVertifyPlugin
                                                                                  .class
                                                                                  .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);
    return apiContext;
  }

}
