package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.eventbus.EventbusRpcRequest;
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
public class EventbusRequestFilterTest {
  private final List<Filter> filters = new ArrayList<>();

  private Vertx vertx;

  private Filter filter;

  private ApiContext apiContext;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();

    JsonObject config = new JsonObject();

    filter = Filter.create(EventBusRequestFilter.class.getSimpleName(), vertx, config);

    filters.clear();
    filters.add(filter);

  }

  @After
  public void tearDown(TestContext testContext) {
//    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testEventbusEndpoint(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.http("get_device", HttpMethod.GET, "devices/", "device");
    EventbusEndpoint reqResp =
        Endpoint.reqResp("send_log", "send_log", new JsonObject().put("action", "get"));
    EventbusEndpoint point =
        Endpoint.reqResp("point", "send_log", null);
    EventbusEndpoint pub =
        Endpoint.reqResp("pub", "send_log", null);
    ApiDefinition definition = ApiDefinition
        .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint, reqResp, point, pub));
    apiContext.setApiDefinition(definition);


    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(3, context.requests().size());
              EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
              testContext.assertEquals("send_log", request.address());
              testContext.assertEquals(1, request.header().size());
              testContext.assertTrue(request.header().containsKey("action"));
              testContext.assertTrue(request.message().isEmpty());
              async.complete();
            }).onFailure(t -> testContext.fail());
  }


  @Test
  public void testEventbusEndpointHasMessage(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    apiContext =
        ApiContext.create(HttpMethod.POST, "/devices", headers, params, new JsonObject().put("foo", "bar"));
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.http("get_device", HttpMethod.GET, "devices/", "device");
    EventbusEndpoint reqResp =
        Endpoint.reqResp("send_log", "send_log", new JsonObject().put("action", "get"));
    EventbusEndpoint point =
        Endpoint.reqResp("point", "send_log", null);
    EventbusEndpoint pub =
        Endpoint.reqResp("pub", "send_log", null);
    ApiDefinition definition = ApiDefinition
        .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint, reqResp, point, pub));
    apiContext.setApiDefinition(definition);


    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(3, context.requests().size());
          EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(2);
          testContext.assertEquals("send_log", request.address());
          testContext.assertEquals(0, request.header().size());
          testContext.assertTrue(request.message().containsKey("foo"));
          async.complete();
        }).onFailure(t -> testContext.fail());
  }

}
