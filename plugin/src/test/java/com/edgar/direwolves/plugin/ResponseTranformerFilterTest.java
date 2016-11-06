package com.edgar.direwolves.plugin;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.plugin.transformer.ResponseTransformerFilter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class ResponseTranformerFilterTest {

  Vertx vertx;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext testContext) {
//    vertx.close(testContext.asyncAssertSuccess());
  }


  @Test
  public void testResponseTransformer(TestContext testContext) {

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition =
        ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);
    JsonObject response = new JsonObject()
        .put("name", "add_device")
        .put("headers", JsonUtils.mutlimapToJson(headers))
        .put("body", new JsonObject());
    apiContext.addResponse(response);
    apiContext.setPrincipal(new JsonObject().put("userId", "1"));

    ResponseTransformerFilter filter = new ResponseTransformerFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        JsonObject jsonObject = apiContext1.response().getJsonObject(0);
        testContext.assertEquals(5, jsonObject.getJsonObject("headers").size());
        testContext.assertEquals(5, jsonObject.getJsonObject("body").size());
      } else {
        testContext.fail();
      }
    });
  }
}