package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.HttpRpcRequest;
import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
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
public class RequestReplaceFilterTest extends FilterTest {

  private final List<Filter> filters = new ArrayList<>();
  RequestReplaceFilter filter;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = new RequestReplaceFilter();
    filter.config(vertx, new JsonObject());

    filters.clear();
    filters.add(filter);
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testEndpointToRequest(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("foo", "query_bar");
    params.put("q7", "$header.h3");
    params.put("q8", "$var.foo");
    params.put("q9", "$body.type");
    params.put("q10", "$user.userId");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h7", "$user.userId");
    headers.put("h8", "$query.foo");
    headers.put("h9", "$var.foo");
    headers.put("h10", "$body.type");

    JsonObject jsonObject = new JsonObject()
        .put("type", 1)
        .put("p7", "$header.h3")
        .put("p8", "$var.foo")
        .put("p9", "$query.foo")
        .put("p10", "$user.userId");

    ApiContext apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
        "add_device")
        .setHost("localhost")
        .setPort(8080)
        .setHttpMethod(HttpMethod.POST)
        .setPath("/")
        .addParam("q3", "v3")
        .addHeader("h3", "v3");
    apiContext.addRequest(httpRpcRequest);

    apiContext.addVariable("foo", "var_bar");

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(1, context.requests().size());
          HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
          testContext.assertEquals("localhost", request.getHost());
          testContext.assertEquals(8080, request.getPort());
          testContext.assertEquals(4, request.getParams());
          testContext.assertEquals(1, request.getParams().get("q7").size());
          testContext.assertEquals("var_bar", request.getParams().get("q8").iterator().next());
          testContext.assertEquals(1, request.getParams().get("q9").iterator().next());
          testContext.assertNull(request.getParams().get("q10"));

          testContext.assertEquals(4, request.getHeaders().size());
          testContext.assertFalse(request.getHeaders().containsKey("h7"));
          testContext.assertEquals("var_bar", request.getHeaders().get("h9").iterator().next());
          testContext.assertEquals("query_bar", request.getHeaders().get("h8").iterator().next());
          testContext.assertEquals(1, request.getHeaders().get("h10").iterator().next());

          testContext.assertNotNull(request.getBody());
          testContext.assertEquals(4, request.getBody().size());
          testContext.assertEquals(2, request.getBody().getJsonArray("p7").size());
          testContext.assertEquals("1", request.getBody().getString("p10"));
          testContext.assertEquals("var_bar", request.getBody().getString("p8"));
          testContext.assertEquals("query_bar", request.getBody().getString("p9"));
          async.complete();
        }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }


}
