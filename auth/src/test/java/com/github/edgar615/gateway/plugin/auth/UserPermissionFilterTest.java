package com.github.edgar615.gateway.plugin.auth;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.plugin.scope.ScopePlugin;
import com.github.edgar615.gateway.core.plugin.scope.ScopePluginImpl;
import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
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
 * Created by edgar on 16-12-25.
 */
@RunWith(VertxUnitRunner.class)
public class UserPermissionFilterTest {

    private final List<Filter> filters = new ArrayList<>();

    private Filter filter;

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();

        filter = Filter.create(UserPermissionFilter.class.getSimpleName(), vertx, new JsonObject());
        filters.clear();
        filters.add(filter);

    }

    @Test
    public void missUserShouldPass(TestContext testContext) {
        ApiContext apiContext = createContext();
        apiContext.addVariable("app.permissions", "user.read, device.wirte");
        apiContext.setPrincipal(new JsonObject());
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> async.complete())
                .onFailure(t -> {
                    testContext.fail();
                });
    }

    @Test
    public void invalidUserShouldThrowNoAuthority(TestContext testContext) {
        ApiContext apiContext = createContext();
        apiContext.addVariable("app.permissions", "user.read, device.wirte");
        apiContext.setPrincipal(new JsonObject().put("permissions", "user.write, device.read"));
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> testContext.fail())
                .onFailure(t -> {
                    testContext.assertTrue(t instanceof SystemException);
                    SystemException ex = (SystemException) t;
                    testContext.assertEquals(DefaultErrorCode.PERMISSION_DENIED, ex.getErrorCode());
                    async.complete();
                });
    }

    @Test
    public void validUserShouldPass(TestContext testContext) {
        ApiContext apiContext = createContext();
        apiContext.addVariable("app.permissions", "user.read, device.wirte");
        apiContext.setPrincipal(new JsonObject().put("permissions", "user.read, device.read"));
        apiContext.setPrincipal(new JsonObject());
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> async.complete())
                .onFailure(t -> {
                    testContext.fail();
                });
    }

    @Test
    public void allPermissionShouldPass(TestContext testContext) {
        ApiContext apiContext = createContext();
        apiContext.addVariable("app.permissions", "user.read, device.wirte");
        apiContext.setPrincipal(new JsonObject().put("permissions", "all"));
        apiContext.setPrincipal(new JsonObject());
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> async.complete())
                .onFailure(t -> {
                    testContext.fail();
                });
    }


    private ApiContext createContext() {
        Multimap<String, String> params = ArrayListMultimap.create();

        ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);

        SimpleHttpEndpoint httpEndpoint =
                SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                        80, "localhost");
        ApiDefinition definition = ApiDefinition
                .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
        apiContext.setApiDefinition(definition);
        ScopePluginImpl plugin =
                (ScopePluginImpl) ApiPlugin.create(ScopePlugin.class.getSimpleName());
        plugin.setPermission("user.read");
        definition.addPlugin(plugin);
        return apiContext;
    }
}
