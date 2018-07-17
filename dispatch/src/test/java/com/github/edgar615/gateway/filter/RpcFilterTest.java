package com.github.edgar615.gateway.filter;

import static org.awaitility.Awaitility.await;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.Endpoint;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.rpc.RpcResponse;
import com.github.edgar615.gateway.core.rpc.http.HttpRpcRequest;
import com.github.edgar615.gateway.core.rpc.http.SimpleHttpRequest;
import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.gateway.handler.DeviceHttpVerticle;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
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

  private int port;

  private Vertx vertx;

  private Filter filter;

  private ApiContext apiContext;

  @Before
  public void testSetUp(TestContext testContext) {
    port = Integer.parseInt(Randoms.randomNumber(4));
    vertx = Vertx.vertx();
    AtomicBoolean started = new AtomicBoolean();

    vertx.deployVerticle(DeviceHttpVerticle.class.getName(),
                         new DeploymentOptions().setConfig(new JsonObject().put("port", port))
                                 .setWorker(true),
                         ar -> started.set(true));
    await().until(() -> started.get());

    JsonObject config = new JsonObject()
            .put("maxFailures", 3)
            .put("timeout", 1000);
    filter = Filter.create(RpcFilter.class.getSimpleName(), vertx, new JsonObject()
    .put("circuit.breaker", config));

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
    Endpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", port, "localhost");

    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
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
    Endpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", port, "localhost");

    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
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
            .put("maxFailures", 1)
            .put("resetTimeout", 2000)
            .put("timeout", 500);
    filter = Filter.create(RpcFilter.class.getSimpleName(), vertx, new JsonObject()
            .put("circuit.breaker", config));

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
    Endpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", port, "localhost");

    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
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
      t.printStackTrace();
      testContext.assertTrue(t instanceof SystemException);
      SystemException se = (SystemException) t;
      testContext.assertEquals(DefaultErrorCode.TIME_OUT, se.getErrorCode());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);
    httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", port, "localhost");

    definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
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
      t.printStackTrace();
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
    Endpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", port, "localhost");

    ApiDefinition definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    HttpRpcRequest  httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
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
      t.printStackTrace();
      testContext.fail();
    });
    Awaitility.await().until(() -> check2.get());
  }

  @Test
  public void testFallback(TestContext testContext) {

    JsonObject config = new JsonObject()
            .put("maxFailures", 1)
            .put("resetTimeout", 2000)
            .put("timeout", 500);
    filter = Filter.create(RpcFilter.class.getSimpleName(), vertx, new JsonObject()
            .put("circuit.breaker", config));

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
    Endpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", port, "localhost");

    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
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
      t.printStackTrace();
      testContext.assertTrue(t instanceof SystemException);
      SystemException se = (SystemException) t;
      testContext.assertEquals(DefaultErrorCode.TIME_OUT, se.getErrorCode());
      check1.set(true);
    });
    Awaitility.await().until(() -> check1.get());

    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);
    httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", port, "localhost");

    definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                           "add_device")
            .setHost("localhost")
            .setPort(port)
            .setHttpMethod(HttpMethod.GET)
            .setPath("/devices")
            .addParam("foo", "bar");
    RpcResponse fallback = RpcResponse.create(httpRpcRequest.id(), 202, new JsonObject().put
            ("foo", "bar").encode(), 0);
    httpRpcRequest.setFallback(fallback);
    apiContext.addRequest(httpRpcRequest);
    task = Task.create();
    task.complete(apiContext);
    AtomicBoolean check2 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(1, context.responses().size());
              RpcResponse response = context.responses().get(0);
              testContext.assertEquals("bar", response.responseObject().getString("foo"));
              check2.set(true);
            }).onFailure(t -> {
      testContext.fail();

    });
    Awaitility.await().until(() -> check2.get());

  }

}
