package com.edgar.direwolves.plugin;

import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.plugin.arg.UrlArgValidateFilter;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.utils.EventbusUtils;
import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.plugin.servicediscovery.ServiceDissoveryFilter;
import com.edgar.direwolves.plugin.transformer.RequestTransformerFilter;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
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
public class ServiceDissoveryFilterTest extends FilterTest {
  private Vertx vertx;
  private final List<Filter> filters = new ArrayList<>();
  private Filter filter;
  private ApiContext apiContext;
  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinition definition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/", "default", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    filter = new ServiceDissoveryFilter();
    filter.config(vertx, new JsonObject());

    filters.clear();
    filters.add(filter);

    vertx.eventBus().<String>consumer("service.discovery.select", msg -> {
      String service = msg.body();
      if ("device".equals(service)) {
        Record record = HttpEndpoint.createRecord("device", "localhost", 8080, "/");
        msg.reply(record.toJson());
      } else if ("user".equals(service)) {
        Record record = HttpEndpoint.createRecord("user", "localhost", 8081, "/");
        msg.reply(record.toJson());
      } else {
        EventbusUtils.fail(msg, SystemException.create(DefaultErrorCode.UNKOWN_REMOTE));
      }
    });
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testEndpointToRequest(TestContext testContext) {

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(1, context.requests().size());
          JsonObject request = context.requests().getJsonObject(0);
          testContext.assertEquals("localhost", request.getString("host"));
          testContext.assertEquals(8080, request.getInteger("port"));
          testContext.assertEquals(1, request.getJsonObject("params").size());
          testContext.assertEquals(1, request.getJsonObject("headers").size());
          testContext.assertNull(request.getJsonObject("body"));
          testContext.assertEquals(1, context.actions().size());
          async.complete();
        }).onFailure(t -> testContext.fail());
  }

  @Test
  public void testEndpointToRequest2(TestContext testContext) {

    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");

    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint2 =
        Endpoint.createHttp("get_user", HttpMethod.GET, "users/", "user");

    ApiDefinition definition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/", "default", Lists.newArrayList(httpEndpoint, httpEndpoint2));
    apiContext.setApiDefinition(definition);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(2, context.requests().size());
          JsonObject request = context.requests().getJsonObject(0);
          testContext.assertEquals("localhost", request.getString("host"));
          testContext.assertEquals(8080, request.getInteger("port"));
          testContext.assertEquals(1, request.getJsonObject("params").size());
          testContext.assertEquals(1, request.getJsonObject("headers").size());
          testContext.assertNull(request.getJsonObject("body"));

          request = context.requests().getJsonObject(1);
          testContext.assertEquals("localhost", request.getString("host"));
          testContext.assertEquals(8081, request.getInteger("port"));
          testContext.assertEquals(1, request.getJsonObject("params").size());
          testContext.assertEquals(1, request.getJsonObject("headers").size());
          testContext.assertNull(request.getJsonObject("body"));

          testContext.assertEquals(1, context.actions().size());
          async.complete();
        }).onFailure(t -> testContext.fail());
  }

  @Test
  public void testNoService(TestContext testContext) {

    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "sms");

    ApiDefinition definition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/", "default", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> testContext.fail())
        .onFailure(t -> {
          testContext.assertTrue(t instanceof ReplyException);
          ReplyException ex = (ReplyException) t;
          testContext.assertEquals(DefaultErrorCode.UNKOWN_REMOTE.getNumber(), ex.failureCode());
          async.complete();
        });
  }

}
