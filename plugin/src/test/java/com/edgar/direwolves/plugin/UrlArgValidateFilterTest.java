package com.edgar.direwolves.plugin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.plugin.arg.UrlArgValidateFilter;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by edgar on 16-10-28.
 */
@RunWith(VertxUnitRunner.class)
public class UrlArgValidateFilterTest {

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @Test
  public void testSuccess(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);


    UrlArgValidateFilter filter = new UrlArgValidateFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        testContext.assertTrue(apiContext1.params().containsKey("limit"));
        testContext.assertEquals("10", Iterables.get(apiContext1.params().get("limit"), 0));
        testContext.assertEquals("0", Iterables.get(apiContext1.params().get("start"), 0));
        async.complete();
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testException(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    ApiDefinition definition =
            ApiDefinition.fromJson(
                    JsonUtils.getJsonFromFile("src/test/resources/app_key_required.json"));
    apiContext.setApiDefinition(definition);


    UrlArgValidateFilter filter = new UrlArgValidateFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    try {
      filter.doFilter(apiContext, future);
      Assert.fail();
    } catch (Exception e) {
      Assert.assertTrue(e instanceof ValidationException);
    }
  }
}
