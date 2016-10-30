package com.edgar.direwolves.plugin;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
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

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class ServiceDescoveryFilterTest {

  Vertx vertx;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();

    vertx.eventBus().consumer("service.discovery.select", msg -> {
      String service = msg.address();
      Record record =HttpEndpoint.createRecord("device", "localhost", 8080, "/");
      msg.reply(record.toJson());
    });

  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }


  @Test
  public void testService(TestContext testContext) {

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, null, null);
    ApiDefinition definition = ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    ServiceDiscoveryFilter filter = new ServiceDiscoveryFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        testContext.assertEquals(1, apiContext1.services().size());
        System.out.println(apiContext1.services().get(0));
        async.complete();
      } else {
        testContext.fail();
        async.complete();
      }
    });
  }

  @Test
  public void testFailed(TestContext testContext) {

    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, null, null);
    ApiDefinition definition = ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add_2endpoint.json"));
    apiContext.setApiDefinition(definition);

    ServiceDiscoveryFilter filter = new ServiceDiscoveryFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        testContext.assertTrue(throwable instanceof SystemException);
        SystemException ex = (SystemException) throwable;
        testContext.assertEquals(DefaultErrorCode.UNKOWN_REMOTE, ex.getErrorCode());
      }
    });
  }

}
