package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.eventbus.EventbusRpcRequest;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.ArrayListMultimap;
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
import java.util.UUID;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class EventbusRequestReplaceFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filters.clear();
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }


  @Test
  public void testReplaceHeader(TestContext testContext) {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("foo", "bar");

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q1", "q1.1");
    params.put("q1", "q1.2");

    JsonObject jsonObject = new JsonObject()
            .put("type", 1)
            .put("obj", new JsonObject()
                    .put("userId", 1)
                    .put("username", "edgar")
                    .put("q1", "$query.q1"))
            .put("arr", new JsonArray().add(1).add("2"));

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    JsonObject header = new JsonObject()
    .put("h1", "$query.q1")
        .put("h2", "$var.foo")
        .put("h3", "$body.type")
        .put("h4", "$user.userId")
        .put("h5", "$var.bar")
        .put("h6", "$body.obj")
        .put("h7", "$body.arr");
    apiContext.addRequest(EventbusRpcRequest.create("a", "send_log", "send_log", EventbusEndpoint.REQ_RESP, header, new JsonObject()));
    apiContext.addRequest(EventbusRpcRequest.create("b", "send_log", "point", EventbusEndpoint.POINT_POINT, header, new JsonObject()));
    apiContext.addRequest(EventbusRpcRequest.create("c", "send_log", "pub", EventbusEndpoint.PUB_SUB, header, new JsonObject()));

    apiContext.addVariable("foo", "var_bar");
    apiContext.setPrincipal(new JsonObject().put("userId", 1));

    Filter filter =
            Filter.create(EventbusRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(3, context.requests().size());
              EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
              System.out.println(request.header());
              testContext.assertEquals(6, request.header().size());
              testContext.assertEquals("q1.1", request.header().getJsonArray("h1").getString(0));
              testContext.assertEquals("var_bar", request.header().getString("h2"));
              testContext.assertEquals(1, request.header().getInteger("h3"));
              testContext.assertEquals(1, request.header().getInteger("h4"));
              testContext.assertFalse(request.header().containsKey("h5"));
              testContext.assertEquals(1, request.header().getJsonObject("h6").getInteger("userId"));
              testContext.assertEquals(1, request.header().getJsonArray("h7").getValue(0));
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }

  @Test
  public void testReplaceBody(TestContext testContext) {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1.1");
    headers.put("h1", "h1.2");
    headers.put("h2", "h2");

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q1", "q1.1");
    params.put("q1", "q1.2");
    params.put("q2", "q2");

    JsonObject jsonObject = new JsonObject()
            .put("type", 1)
            .put("obj", new JsonObject()
                    .put("userId", 1)
                    .put("username", "edgar")
                    .put("q1", "$query.q1"))
            .put("arr", new JsonArray().add(1).add("2"));

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    apiContext.addRequest(EventbusRpcRequest.create("a", "send_log", "send_log", EventbusEndpoint.REQ_RESP, null, jsonObject));
    apiContext.addRequest(EventbusRpcRequest.create("b", "send_log", "point", EventbusEndpoint.POINT_POINT, null, jsonObject));
    apiContext.addRequest(EventbusRpcRequest.create("c", "send_log", "pub", EventbusEndpoint.PUB_SUB, null, jsonObject));

    apiContext.addVariable("foo", "var_bar");
    apiContext.setPrincipal(new JsonObject().put("userId", 1));

    Filter filter =
            Filter.create(RequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(1, context.requests().size());
              EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
              System.out.println(request.message());
              testContext.assertEquals(7, request.message().size());
              testContext.assertEquals(2, request.message().getJsonArray("b1").size());
              testContext.assertEquals(2, request.message().getJsonArray("b2").size());
              testContext.assertEquals("var_bar", request.message().getString("b3"));
              testContext.assertEquals(1, request.message().getInteger("b4"));
              testContext.assertFalse(request.message().containsKey("b5"));
              testContext.assertEquals(3, request.message().getJsonObject("b6").size());
              testContext.assertEquals(1, request.message().getJsonObject("b6").getInteger("userId"));
              testContext.assertEquals(2, request.message().getJsonObject("b6").getJsonArray("q1")
                      .size());
              testContext.assertEquals(2, request.message().getJsonArray("b7").size());
              testContext.assertEquals(1, request.message().getJsonArray("b7").iterator().next());

              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }


}
