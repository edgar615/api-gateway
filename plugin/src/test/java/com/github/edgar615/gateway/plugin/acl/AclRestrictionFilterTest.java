package com.github.edgar615.gateway.plugin.acl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.utils.Filters;
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
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by edgar on 16-10-28.
 */
@RunWith(VertxUnitRunner.class)
public class AclRestrictionFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Filter filter;

  private ApiContext apiContext;

  private String groupKey = "group";

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
    apiContext.setPrincipal(new JsonObject().put(groupKey, "testGroup"));

    JsonObject config = new JsonObject().put("blacklist", new JsonArray().add("guest"))
            .put("whitelist", new JsonArray().add("group1"));
    filter = Filter.create(AclRestrictionFilter.class.getSimpleName(), Vertx.vertx(),
                           new JsonObject()
                                   .put("acl.restriction", config));

    filters.clear();
    filters.add(filter);
  }

  @Test
  public void testNoGroupShouldSuccess(TestContext testContext) {
    AclRestriction plugin =
            (AclRestriction) ApiPlugin.create(AclRestriction.class.getSimpleName());
    plugin.addBlacklist("testGroup");
    apiContext.setPrincipal(null);
    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
//      SystemException ex = (SystemException) t;
//      testContext.assertEquals(DefaultErrorCode.PERMISSION_DENIED, ex.getErrorCode());
//      async.complete();
    });
  }

  @Test
  public void testGlobalBlackGroupShouldForbidden(TestContext testContext) {
//    UserRestriction plugin =
//            (UserRestriction) ApiPlugin.create(UserRestriction.class.getSimpleName());
//    plugin.addBlacklist("testGroup");
//    apiContext.apiDefinition().addPlugin(plugin);
    apiContext.setPrincipal(new JsonObject().put(groupKey, "guest"));
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
  public void testGlobalWhiteShouldAlwaysAllow(TestContext testContext) {
    AclRestriction plugin =
            (AclRestriction) ApiPlugin.create(AclRestriction.class.getSimpleName());
    plugin.addBlacklist("group1");
    apiContext.setPrincipal(new JsonObject().put(groupKey, "group1"));
    apiContext.apiDefinition().addPlugin(plugin);
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
  public void testBlackGroupShouldForbidden(TestContext testContext) {
    AclRestriction plugin =
            (AclRestriction) ApiPlugin.create(AclRestriction.class.getSimpleName());
    plugin.addBlacklist("testGroup");
    apiContext.apiDefinition().addPlugin(plugin);
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
  public void testWildcardBlackShouldForbidden(TestContext testContext) {
    AclRestriction plugin =
            (AclRestriction) ApiPlugin.create(AclRestriction.class.getSimpleName());
    plugin.addBlacklist("*");
    apiContext.apiDefinition().addPlugin(plugin);
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
  public void testWhiteShouldAlwaysAllow(TestContext testContext) {
    AclRestriction plugin =
            (AclRestriction) ApiPlugin.create(AclRestriction.class.getSimpleName());
    plugin.addBlacklist("testGroup");
    plugin.addWhitelist("testGroup");
    apiContext.apiDefinition().addPlugin(plugin);
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
  public void testWildcardWhiteShouldAlwaysAllow(TestContext testContext) {
    AclRestriction plugin =
            (AclRestriction) ApiPlugin.create(AclRestriction.class.getSimpleName());
    plugin.addBlacklist("testGroup");
    plugin.addWhitelist("*");
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
  public void testUpdateGlobalConfig(TestContext testContext) {
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    AtomicBoolean check1 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              check1.set(true);

            }).onFailure(t -> {
      testContext.fail();
    });
    Awaitility.await().until(() -> check1.get());

    JsonObject restriction = new JsonObject().put("blacklist", new JsonArray().add("testGroup"));

    filter.updateConfig(new JsonObject().put("acl.restriction", restriction));
    task = Task.create();
    task.complete(apiContext);
    AtomicBoolean check2 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            }).onFailure(t -> {
      t.printStackTrace();
      SystemException ex = (SystemException) t;
      testContext.assertEquals(DefaultErrorCode.PERMISSION_DENIED, ex.getErrorCode());
      check2.set(true);
    });
    Awaitility.await().until(() -> check2.get());

  }

}
