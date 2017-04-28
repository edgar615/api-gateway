package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.direwolves.filter.servicediscovery.MockConsulHttpVerticle;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/11/18.
 *
 * @author Edgar  Date 2016/11/18
 */
@RunWith(VertxUnitRunner.class)
public class ServiceDiscoveryFilterTest {
  private final List<Filter> filters = new ArrayList<>();

  MockConsulHttpVerticle mockConsulHttpVerticle;

  private Vertx vertx;

  private Filter filter;

  private ApiContext apiContext;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();
    mockConsulHttpVerticle = new MockConsulHttpVerticle();
    Async async = testContext.async();
    vertx.deployVerticle(mockConsulHttpVerticle, ar -> {
      async.complete();
    });

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
            HttpEndpoint.http("get_device", HttpMethod.GET, "devices/", "device");

    EventbusEndpoint eventbusEndpoint =
        EventbusEndpoint.reqResp("send_log","send_log", null);
    ApiDefinition definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint, eventbusEndpoint));
    apiContext.setApiDefinition(definition);

    JsonObject config = new JsonObject()
            .put("service.discovery", "consul://localhost:5601");
    JsonObject strategy = new JsonObject();
    config.put("service.discovery.select-strategy", strategy);

    filter = Filter.create(ServiceDiscoveryFilter.class.getSimpleName(), vertx, config);

    filters.clear();
    filters.add(filter);

  }

  @After
  public void tearDown(TestContext testContext) {
//    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void singleEndpointShouldReturnSingleRequest(TestContext testContext) {
    add2Servers();
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(1, request.params().keySet().size());
              testContext.assertEquals(1, request.headers().keySet().size());
              testContext.assertTrue(request.headers().containsKey("x-request-id"));
              testContext.assertNull(request.body());
              async.complete();
            }).onFailure(t -> testContext.fail());
  }

  @Test
  public void twoEndpointShouldReturnTwoRequest(TestContext testContext) {
    add2Servers();
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
            HttpEndpoint.http("get_device", HttpMethod.GET, "devices/", "device");

    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint2 =
            HttpEndpoint.http("get_user", HttpMethod.GET, "users/", "user");

    ApiDefinition definition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/",
                                                    Lists.newArrayList(httpEndpoint,
                                                                       httpEndpoint2));
    apiContext.setApiDefinition(definition);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(2, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(1, request.params().keySet().size());
              testContext.assertEquals(1, request.headers().keySet().size());
              testContext.assertTrue(request.headers().containsKey("x-request-id"));
              testContext.assertNull(request.body());

              request = (HttpRpcRequest) context.requests().get(1);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8081, request.port());
              testContext.assertEquals(1, request.params().keySet().size());
              testContext.assertEquals(1, request.headers().keySet().size());
              testContext.assertTrue(request.headers().containsKey("x-request-id"));
              testContext.assertNull(request.body());

              async.complete();
            }).onFailure(t -> testContext.fail());
  }

//  @Test
//  public void testRoundRobin(TestContext testContext) {
//    add2Servers2();
//
//    Multimap<Integer, JsonObject> group = ArrayListMultimap.create();
//    for (int i = 0; i < 20; i++) {
//      Async async = testContext.async();
//      Task<ApiContext> task = Task.create();
//      task.complete(apiContext);
//      doFilter(task, filters)
//          .andThen(context -> {
//            JsonObject request = context.requests().getJsonObject(0);
//            group.put(request.port(), request);
//            async.complete();
//          }).onFailure(t -> testContext.fail());
//    }
//
//    await().until(() -> group.size() == 20);
//    Assert.assertEquals(10, group.get(32769).size());
//    Assert.assertEquals(10, group.get(32770).size());
//
//  }

  @Test
  public void testNoService(TestContext testContext) {
    add2Servers();
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
            HttpEndpoint.http("get_device", HttpMethod.GET, "devices/", "sms");

    ApiDefinition definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> testContext.fail())
            .onFailure(t -> {
              testContext.assertTrue(t instanceof SystemException);
              SystemException ex = (SystemException) t;
              testContext.assertEquals(DefaultErrorCode.SERVICE_UNAVAILABLE.getNumber(),
                                       ex.getErrorCode().getNumber());
              async.complete();
            });
  }

  private void add2Servers() {
    mockConsulHttpVerticle.addService(new JsonObject()
                                              .put("Node", "u221")
                                              .put("Address", "localhost")
                                              .put("ServiceID", "u221:device:8080")
                                              .put("ServiceName", "device")
                                              .put("ServiceTags", new JsonArray())
                                              .put("ServicePort", 8080));
    mockConsulHttpVerticle.addService((new JsonObject()
            .put("Node", "u222")
            .put("Address", "localhost")
            .put("ServiceID", "u222:device:8080")
            .put("ServiceName", "user")
            .put("ServiceTags", new JsonArray())
            .put("ServicePort", 8081)));
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
