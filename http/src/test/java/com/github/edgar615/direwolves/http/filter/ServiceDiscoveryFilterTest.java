package com.github.edgar615.direwolves.http.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.EventbusEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.rpc.http.HttpRpcRequest;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.direwolves.http.SdHttpEndpoint;
import com.github.edgar615.direwolves.http.SdHttpRequest;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.consul.ConsulServiceImporter;
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
public class ServiceDiscoveryFilterTest {
  private final List<Filter> filters = new ArrayList<>();

  MockConsulHttpVerticle mockConsulHttpVerticle;

  private Vertx vertx;

  private Filter filter;

  private ApiContext apiContext;

  private ServiceDiscovery discovery;

  private ConsulServiceImporter importer;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();
    discovery = ServiceDiscovery.create(vertx);
    importer = new ConsulServiceImporter();
    discovery.registerServiceImporter(importer, new JsonObject()
            .put("host", "localhost")
            .put("port", 5601));

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
    SdHttpEndpoint httpEndpoint =
            SdHttpEndpoint.http("get_device", HttpMethod.GET, "devices/", "device");

    EventbusEndpoint eventbusEndpoint =
            EventbusEndpoint.reqResp("send_log", "send_log", null, null);
    ApiDefinition definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/",
                    Lists.newArrayList(httpEndpoint, eventbusEndpoint));
    apiContext.setApiDefinition(definition);

    JsonObject config = new JsonObject();
    JsonObject loadBalanceConfig = new JsonObject();
    loadBalanceConfig.put("strategy", new JsonObject());
    config.put("service.discovery", new JsonObject()
            .put("load.balance", loadBalanceConfig));


    filter = Filter.create(ServiceDiscoveryFilter.class.getSimpleName(), vertx, config);

    filters.clear();
    filters.add(filter);

  }

  @After
  public void tearDown(TestContext testContext) {
//    vertx.close(testContext.asyncAssertSuccess());
    AtomicBoolean complete = new AtomicBoolean();
    importer.close(ar -> {
      complete.set(true);
    });
    Awaitility.await().until(() -> complete.set(true));
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
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void twoEndpointShouldReturnTwoRequest(TestContext testContext) {
    add2Servers();
    SdHttpEndpoint httpEndpoint =
            SdHttpEndpoint.http("get_device", HttpMethod.GET, "devices/", "device");

    SdHttpEndpoint httpEndpoint2 =
            SdHttpEndpoint.http("get_user", HttpMethod.GET, "users/", "user");

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

  @Test
  public void testNoService(TestContext testContext) {
    add2Servers();
    SdHttpEndpoint httpEndpoint =
            SdHttpEndpoint.http("get_device", HttpMethod.GET, "devices/", "sms");

    ApiDefinition definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(1, context.requests().size());
              SdHttpRequest request = (SdHttpRequest) context.requests().get(0);
              testContext.assertEquals(1, request.params().keySet().size());
              testContext.assertEquals(1, request.headers().keySet().size());
              testContext.assertTrue(request.headers().containsKey("x-request-id"));
              testContext.assertNull(request.body());
              testContext.assertNull(request.record());

              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  private void add2Servers() {
    mockConsulHttpVerticle.addService(new JsonObject()
                                              .put("ID", UUID.randomUUID().toString())
                                              .put("Node", "u221")
                                              .put("Address", "localhost")
                                              .put("ServiceID", "u221:device:8080")
                                              .put("ServiceName", "device")
                                              .put("ServiceTags", new JsonArray())
                                              .put("ServicePort", 8080));
    mockConsulHttpVerticle.addService((new JsonObject()
            .put("ID", UUID.randomUUID().toString())
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
