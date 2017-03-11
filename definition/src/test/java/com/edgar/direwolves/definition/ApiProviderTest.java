package com.edgar.direwolves.definition;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiProvider;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.exception.DefaultErrorCode;
import com.google.common.collect.Lists;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2017/1/3.
 *
 * @author Edgar  Date 2017/1/3
 */
@RunWith(VertxUnitRunner.class)
public class ApiProviderTest {

  ApiProvider apiProvider;

  @Before
  public void setUp(TestContext testContext) {
    apiProvider = new ApiProviderImpl();
    Endpoint httpEndpoint = HttpEndpoint.http("add_device", HttpMethod.GET, "/devices",
        "device");
    ApiDefinition apiDefinition = ApiDefinition.create("add_device", HttpMethod.POST, "/devices",
                                                       Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);
    apiDefinition = ApiDefinition.create("update_device", HttpMethod.PUT, "/devices",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);
    apiDefinition = ApiDefinition.create("update_device2", HttpMethod.PUT, "/devices",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);
  }

  @Test
  public void testMatch(TestContext testContext) {
    Async async = testContext.async();
    apiProvider.match("post", "/devices", ar -> {
      if (ar.succeeded()) {
        JsonObject jsonObject = ar.result();
        testContext.assertTrue(jsonObject.containsKey("name"));
        async.complete();
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testNoApiFound(TestContext testContext) {
    Async async = testContext.async();
    apiProvider.match("get", "/devices", ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        testContext.assertTrue(throwable instanceof ReplyException);
        ReplyException ex = (ReplyException) throwable;
        testContext.assertEquals(DefaultErrorCode.RESOURCE_NOT_FOUND.getNumber(), ex.failureCode());
        async.complete();
      }
    });
  }

  @Test
  public void testDuplicateShouldThrowResourceNotFound(TestContext testContext) {
    Async async = testContext.async();
    apiProvider.match("put", "/devices", ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        testContext.assertTrue(throwable instanceof ReplyException);
        ReplyException ex = (ReplyException) throwable;
        testContext.assertEquals(DefaultErrorCode.RESOURCE_NOT_FOUND.getNumber(), ex.failureCode());
        async.complete();
      }
    });
  }
}
