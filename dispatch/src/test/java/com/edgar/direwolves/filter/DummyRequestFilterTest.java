package com.edgar.direwolves.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.DummyEndpoint;
import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.dummy.DummyRequest;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.vertx.task.Task;
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
 * Created by Edgar on 2016/11/18.
 *
 * @author Edgar  Date 2016/11/18
 */
@RunWith(VertxUnitRunner.class)
public class DummyRequestFilterTest {
  private final List<Filter> filters = new ArrayList<>();

  private Vertx vertx;

  private Filter filter;

  private ApiContext apiContext;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();

    JsonObject config = new JsonObject();

    filter = Filter.create(DummyRequestFilter.class.getSimpleName(), vertx, config);

    filters.clear();
    filters.add(filter);

  }

  @After
  public void tearDown(TestContext testContext) {
//    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testDummyEndpoint(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    Multimap<String, String> ebHeaders = ArrayListMultimap.create();
    ebHeaders.put("action", "get");

    apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    HttpEndpoint httpEndpoint =
        HttpEndpoint.http("get_device", HttpMethod.GET, "devices/", "device");
    DummyEndpoint dummyEndpoint =
        DummyEndpoint.dummy("dummy", new JsonObject().put("result", 1));
    EventbusEndpoint point =
        EventbusEndpoint.pointToPoint("point", "send_log", null, null);
    ApiDefinition definition = ApiDefinition
        .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint, point, dummyEndpoint));
    apiContext.setApiDefinition(definition);


    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(1, context.requests().size());
          DummyRequest request = (DummyRequest) context.requests().get(0);
          System.out.println(request);
          testContext.assertEquals("dummy", request.name());
          testContext.assertEquals(1, request.result().getInteger("result"));
          async.complete();
        }).onFailure(t -> testContext.fail()
    );
  }

}
