package com.edgar.direwolves.plugin.ip;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edgar on 16-10-28.
 */
@RunWith(VertxUnitRunner.class)
public class IpRestrictionFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Filter filter;

  private ApiContext apiContext;

  @Before
  public void setUp() {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    HttpEndpoint httpEndpoint =
            HttpEndpoint.http("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinition definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    filter = Filter.create(IpRestrictionFilter.class.getSimpleName(), Vertx.vertx(),
                           new JsonObject());

    filters.clear();
    filters.add(filter);
  }

  @Test
  public void testOrderAndType(TestContext testContext) {
    Assert.assertEquals(100, filter.order());
    Assert.assertEquals(Filter.PRE, filter.type());
  }

  @Test
  public void testBlackIpShouldForbidden(TestContext testContext) {
    IpRestriction plugin = (IpRestriction) ApiPlugin.create(IpRestriction.class.getSimpleName());
    plugin.addBlacklist("10.4.7.15");
    plugin.addBlacklist("192.168.1.100");
    apiContext.apiDefinition().addPlugin(plugin);
    apiContext.addVariable("request.client_ip", "10.4.7.15");
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            }).onFailure(t -> {
      SystemException ex = (SystemException) t;
      testContext.assertEquals(DefaultErrorCode.PERMISSION_DENIED, ex.getErrorCode());
      async.complete();
    });
  }

  @Test
  public void testWildcardBlackIpShouldForbidden(TestContext testContext) {
    IpRestriction plugin = (IpRestriction) ApiPlugin.create(IpRestriction.class.getSimpleName());
    plugin.addBlacklist("10.4.*.15");
    apiContext.apiDefinition().addPlugin(plugin);
    apiContext.addVariable("request.client_ip", "10.4.87.15");
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            }).onFailure(t -> {
      SystemException ex = (SystemException) t;
      testContext.assertEquals(DefaultErrorCode.PERMISSION_DENIED, ex.getErrorCode());
      async.complete();
    });
  }

  @Test
  public void testWhiteIpShouldAlwaysAllow(TestContext testContext) {
    IpRestriction plugin = (IpRestriction) ApiPlugin.create(IpRestriction.class.getSimpleName());
    plugin.addBlacklist("10.4.7.15");
    plugin.addWhitelist("10.4.7.15");
    apiContext.apiDefinition().addPlugin(plugin);
    apiContext.addVariable("request.client_ip", "10.4.7.15");
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              async.complete();
            }).onFailure(t -> {
      testContext.fail();
    });
  }

  @Test
  public void testWildcardWhiteIpShouldAlwaysAllow(TestContext testContext) {
    IpRestriction plugin = (IpRestriction) ApiPlugin.create(IpRestriction.class.getSimpleName());
    plugin.addBlacklist("10.4.7.15");
    plugin.addWhitelist("10.*.7.*");
    apiContext.apiDefinition().addPlugin(plugin);
    apiContext.addVariable("request.client_ip", "10.4.7.15");
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              async.complete();
            }).onFailure(t -> {
      testContext.fail();
    });
  }

}
