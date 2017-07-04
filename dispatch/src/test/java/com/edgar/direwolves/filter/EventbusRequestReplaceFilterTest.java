package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.eventbus.EventbusRpcRequest;
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

    Multimap<String, String> ebheaders = ArrayListMultimap.create();
    ebheaders.put("h1", "$query.q1");
    ebheaders .put("h2", "$var.foo");
    ebheaders.put("h3", "$body.type");
    ebheaders .put("h4", "$user.userId");
    ebheaders.put("h5", "$var.bar");
    ebheaders.put("h6", "$body.obj");
    ebheaders .put("h7", "$body.arr");;
    apiContext.addRequest(EventbusRpcRequest.create("a", "send_log", "send_log", EventbusEndpoint
            .REQ_RESP, null,ebheaders, new JsonObject()));
    apiContext.addRequest(EventbusRpcRequest.create("b", "send_log", "point", EventbusEndpoint
            .POINT_POINT, null, ebheaders, new JsonObject()));
    apiContext.addRequest(EventbusRpcRequest.create("c", "send_log", "pub", EventbusEndpoint.PUB_SUB,
                                                    null, ebheaders, new JsonObject()));

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
              System.out.println(request.headers());
              testContext.assertEquals(6, request.headers().keySet().size());
              testContext.assertEquals("q1.1", request.headers().get("h1").iterator().next());
              testContext.assertEquals("var_bar", request.headers().get("h2").iterator().next());
              testContext.assertEquals("1", request.headers().get("h3").iterator().next());
              testContext.assertEquals("1", request.headers().get("h4").iterator().next());
              testContext.assertTrue(request.headers().get("h5").isEmpty());
              testContext.assertEquals(1, request.headers().get("h6").size());
              testContext.assertEquals("1", request.headers().get("h7").iterator().next());
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
        .put("b1", "$header.h1")
        .put("b2", "$query.q1")
        .put("b3", "$var.foo")
        .put("b4", "$user.userId")
        .put("b5", "$var.bar")
        .put("b6", new JsonObject()
            .put("userId", 1)
            .put("username", "edgar")
            .put("q1", "$query.q1"))
        .put("b7", new JsonArray().add("$user.userId").add("2"))
        .put("foo", "bar");

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    apiContext.addRequest(EventbusRpcRequest.create("a", "send_log", "send_log", EventbusEndpoint
            .REQ_RESP, null,null, jsonObject));
    apiContext.addRequest(EventbusRpcRequest.create("b", "send_log", "point", EventbusEndpoint.POINT_POINT, null, null,jsonObject));
    apiContext.addRequest(EventbusRpcRequest.create("c", "send_log", "pub", EventbusEndpoint.PUB_SUB, null,null, jsonObject));

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
