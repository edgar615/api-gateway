package com.edgar.direwolves.plugin.transformer;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.eventbus.EventbusRpcRequest;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
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
public class EventbusRequestTransformerFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  EventbusRequestTransformerFilter filter;

  private ApiContext apiContext;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = new EventbusRequestTransformerFilter();

    filters.clear();
    filters.add(filter);

    createApiContext();
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }


  @Test
  public void testOrderAndType(TestContext testContext) {
    Assert.assertEquals(10000, filter.order());
    Assert.assertEquals(Filter.PRE, filter.type());
  }

  @Test
  public void testSingleRequestTransformer(TestContext testContext) {

    RequestTransformer transformer = createRequestTransformer();
    RequestTransformerPlugin plugin = (RequestTransformerPlugin) ApiPlugin
            .create(RequestTransformerPlugin.class.getSimpleName());
    plugin.addTransformer(transformer);
    Multimap<String, String> ebHeaders = ArrayListMultimap.create();
    ebHeaders.put("h3", "v3");

    JsonObject jsonObject = new JsonObject()
        .put("b3","v3");

    apiContext.addRequest(EventbusRpcRequest.create("a", "send_log", "send_log", EventbusEndpoint.REQ_RESP, ebHeaders, jsonObject));

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(1, context.requests().size());
              EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
              testContext.assertEquals(2, request.headers().size());
              testContext.assertFalse(request.headers().containsKey("h3"));
              testContext.assertEquals(2, request.message().size());
              System.out.println(request);
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void testTwoRequestTransformer(TestContext testContext) {

    RequestTransformer transformer = createRequestTransformer();

    RequestTransformerPlugin plugin = (RequestTransformerPlugin) ApiPlugin
            .create(RequestTransformerPlugin.class.getSimpleName());
    plugin.addTransformer(transformer);
    Multimap<String, String> ebHeaders = ArrayListMultimap.create();
    ebHeaders.put("h3", "v3");

    JsonObject jsonObject = new JsonObject()
        .put("b3","v3");

    apiContext.addRequest(EventbusRpcRequest.create("a", "send_log", "send_log", EventbusEndpoint.REQ_RESP, ebHeaders, jsonObject));

    apiContext.addRequest(EventbusRpcRequest.create("a", "send_log2", "send_log", EventbusEndpoint.REQ_RESP, ebHeaders, jsonObject));

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(2, context.requests().size());
              EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
              System.out.println(request);
              testContext.assertEquals(2, request.headers().size());
              testContext.assertFalse(request.headers().containsKey("h3"));
              testContext.assertEquals(2, request.message().size());

              request = (EventbusRpcRequest) context.requests().get(1);
              System.out.println(request);
              testContext.assertEquals(1, request.headers().size());
              testContext.assertTrue(request.headers().containsKey("h3"));
              testContext.assertEquals(1, request.message().size());
              async.complete();
            }).onFailure(t -> testContext.fail());
  }

  private void createApiContext() {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
            HttpEndpoint.http("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists
            .newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
  }

  private RequestTransformer createRequestTransformer() {
    RequestTransformer transformer = RequestTransformer.create("send_log");
    transformer.removeHeader("h3");
    transformer.removeHeader("h4");
    transformer.removeParam("q3");
    transformer.removeParam("q4");
    transformer.removeBody("b3");
    transformer.removeBody("b4");
    transformer.addHeader("h2", "h2");
    transformer.addHeader("h1", "h1");
    transformer.addParam("q1", "q1");
    transformer.addParam("q2", "q2");
    transformer.addBody("b1", "b1");
    transformer.addBody("b2", "b2");
    return transformer;
  }

}
