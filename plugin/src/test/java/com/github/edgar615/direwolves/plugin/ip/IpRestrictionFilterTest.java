package com.github.edgar615.direwolves.plugin.ip;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                    80, "localhost");

    ApiDefinition definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    JsonObject config = new JsonObject().put("blacklist", new JsonArray().add("86.10.*"))
            .put("whitelist", new JsonArray().add("86.10.2.*"));
    filter = Filter.create(IpRestrictionFilter.class.getSimpleName(), Vertx.vertx(),
                           new JsonObject().put("ip.restriction", config));

    filters.clear();
    filters.add(filter);
  }

  @Test
  public void testGlobalBlackIpShouldForbidden(TestContext testContext) {
//    IpRestriction plugin = (IpRestriction) ApiPlugin.create(IpRestriction.class.getSimpleName());
//    plugin.addBlacklist("10.4.7.15");
//    plugin.addBlacklist("192.168.1.100");
//    apiContext.apiDefinition().addPlugin(plugin);
    apiContext.addVariable("request.clientIp", "86.10.1.1");
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
  public void testGlobalWhiteIpShouldAlwaysAllow(TestContext testContext) {
    IpRestriction plugin = (IpRestriction) ApiPlugin.create(IpRestriction.class.getSimpleName());
    plugin.addBlacklist("10.4.7.15");
    plugin.addWhitelist("10.4.7.15");
    apiContext.apiDefinition().addPlugin(plugin);
    apiContext.addVariable("request.clientIp", "86.10.2.100");
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
  public void testBlackIpShouldForbidden(TestContext testContext) {
    IpRestriction plugin = (IpRestriction) ApiPlugin.create(IpRestriction.class.getSimpleName());
    plugin.addBlacklist("10.4.7.15");
    plugin.addBlacklist("192.168.1.100");
    apiContext.apiDefinition().addPlugin(plugin);
    apiContext.addVariable("request.clientIp", "10.4.7.15");
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
    apiContext.addVariable("request.clientIp", "10.4.87.15");
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
    apiContext.addVariable("request.clientIp", "10.4.7.15");
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
    apiContext.addVariable("request.clientIp", "10.4.7.15");
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
