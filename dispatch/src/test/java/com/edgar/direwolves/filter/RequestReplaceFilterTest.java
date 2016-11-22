package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
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
    apiContext.addRequest(new JsonObject()
        .put("name", "add_device")
        .put("host", "localhost")
        .put("port", 8080)
        .put("method", "post")
        .put("params", JsonUtils.mutlimapToJson(params))
        .put("headers", JsonUtils.mutlimapToJson(headers))
        .put("body", jsonObject));

    apiContext.addVariable("foo", "var_bar");

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(1, context.requests().size());
          JsonObject request = context.requests().getJsonObject(0);
          testContext.assertEquals("localhost", request.getString("host"));
          testContext.assertEquals(8080, request.getInteger("port"));
          testContext.assertEquals(4, request.getJsonObject("params").size());
          testContext.assertEquals(1, request.getJsonObject("params").getJsonArray("q7").size());
          testContext.assertEquals("var_bar", request.getJsonObject("params").getJsonArray("q8").getString(0));
          testContext.assertEquals(1, request.getJsonObject("params").getJsonArray("q9").getInteger(0));
          testContext.assertNull(request.getJsonObject("params").getJsonArray("q10"));

          testContext.assertEquals(4, request.getJsonObject("headers").size());
          testContext.assertFalse(request.getJsonObject("headers").containsKey("h7"));
          testContext.assertEquals("var_bar", request.getJsonObject("headers").getJsonArray("h9").getString(0));
          testContext.assertEquals("query_bar", request.getJsonObject("headers").getJsonArray("h8").getString(0));
          testContext.assertEquals(1, request.getJsonObject("headers").getJsonArray("h10").getInteger(0));

          testContext.assertNotNull(request.getJsonObject("body"));
          testContext.assertEquals(4, request.getJsonObject("body").size());
          testContext.assertEquals(2, request.getJsonObject("body").getJsonArray("p7").size());
          testContext.assertEquals("1", request.getJsonObject("body").getString("p10"));
          testContext.assertEquals("var_bar", request.getJsonObject("body").getString("p8"));
          testContext.assertEquals("query_bar", request.getJsonObject("body").getString("p9"));
          async.complete();
        }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }


}
