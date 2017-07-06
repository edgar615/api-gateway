package com.edgar.direwolves.filter;

import static org.awaitility.Awaitility.await;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.direwolves.handler.DeviceHttpVerticle;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.DeploymentOptions;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2016/11/18.
 *
 * @author Edgar  Date 2016/11/18
 */
@RunWith(VertxUnitRunner.class)
public class RpcFilterTest {
  private final List<Filter> filters = new ArrayList<>();

  MockConsulHttpVerticle mockConsulHttpVerticle;

  int port = Integer.parseInt(Randoms.randomNumber(4));

  private Vertx vertx;

  private Filter filter;

  private ApiContext apiContext;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();
    AtomicBoolean started = new AtomicBoolean();

    vertx.deployVerticle(DeviceHttpVerticle.class.getName(),
                         new DeploymentOptions().setConfig(new JsonObject().put("http.port", port))
                                 .setWorker(true),
                         ar -> started.set(true));
    await().until(() -> started.get());

    JsonObject config = new JsonObject()
            .put("circuit.breaker.maxFailures", 3)
            .put("circuit.breaker.timeout", 1000);
    filter = Filter.create(RpcFilter.class.getSimpleName(), vertx, config);

    filters.clear();
    filters.add(filter);

  }

  @After
  public void tearDown(TestContext testContext) {
//    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testHttpRequest(TestContext testContext) {
    String id = UUID.randomUUID().toString();
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("foo", "bar");

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1.1");
    headers.put("h1", "h1.2");

    JsonObject jsonObject = new JsonObject()
            .put("type", 1);

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
                                                          "add_device")
            .setHost("localhost")
            .setServerId(id)
            .setPort(port)
            .setHttpMethod(HttpMethod.GET)
            .setPath("/devices")
            .addParam("foo", "bar");
    apiContext.addRequest(httpRpcRequest);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.responses());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void testTimeOut(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("foo", "bar");

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1.1");
    headers.put("h1", "h1.2");

    JsonObject jsonObject = new JsonObject()
            .put("type", 1);

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
                                                          "add_device")
            .setHost("localhost")
            .setPort(port)
            .setHttpMethod(HttpMethod.GET)
            .setPath("/devices/timeout")
            .addParam("foo", "bar");
    apiContext.addRequest(httpRpcRequest);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.responses());
              testContext.fail();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.assertTrue(t instanceof SystemException);
      SystemException se = (SystemException) t;
      testContext.assertEquals(DefaultErrorCode.TIME_OUT, se.getErrorCode());
      async.complete();
    });
  }

  @Test
  public void testCircuitBreakerOpen(TestContext testContext) {

    JsonObject config = new JsonObject()
            .put("circuit.breaker.maxFailures", 1)
            .put("circuit.breaker.resetTimeout", 2000)
            .put("circuit.breaker.timeout", 500);
    filter = Filter.create(RpcFilter.class.getSimpleName(), vertx, config);

    filters.clear();
    filters.add(filter);

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("foo", "bar");

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1.1");
    headers.put("h1", "h1.2");

    JsonObject jsonObject = new JsonObject()
            .put("type", 1);

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
                                                          "add_device")
            .setHost("localhost")
            .setPort(port)
            .setHttpMethod(HttpMethod.GET)
            .setPath("/devices/timeout")
            .addParam("foo", "bar");
    apiContext.addRequest(httpRpcRequest);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    AtomicBoolean check1 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.responses());
              testContext.fail();
            }).onFailure(t -> {
      testContext.assertTrue(t instanceof SystemException);
      SystemException se = (SystemException) t;
      testContext.assertEquals(DefaultErrorCode.TIME_OUT, se.getErrorCode());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
                                                          "add_device")
            .setHost("localhost")
            .setPort(port)
            .setHttpMethod(HttpMethod.GET)
            .setPath("/devices")
            .addParam("foo", "bar");
    apiContext.addRequest(httpRpcRequest);
    task = Task.create();
    task.complete(apiContext);
    AtomicBoolean check2 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            }).onFailure(t -> {
      testContext.assertTrue(t instanceof SystemException);
      SystemException se = (SystemException) t;
      testContext.assertEquals(DefaultErrorCode.BREAKER_TRIPPED, se.getErrorCode());
      check2.set(true);
    });
    Awaitility.await().until(() -> check2.get());

  }

  @Test
  public void testCircuitBreakerReset(TestContext testContext) {

  testCircuitBreakerOpen(testContext);
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("foo", "bar");

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1.1");
    headers.put("h1", "h1.2");
    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);

    HttpRpcRequest  httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
                                           "add_device")
            .setHost("localhost")
            .setPort(port)
            .setHttpMethod(HttpMethod.GET)
            .setPath("/devices")
            .addParam("foo", "bar");
    apiContext.addRequest(httpRpcRequest);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    AtomicBoolean check2 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.responses());
              check2.set(true);
            }).onFailure(t -> {
      testContext.fail();
    });
    Awaitility.await().until(() -> check2.get());
  }

}
