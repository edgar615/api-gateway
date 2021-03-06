package com.github.edgar615.gateway.plugin.arg;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.ValidationException;
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
 * Created by edgar on 16-10-28.
 */
@RunWith(VertxUnitRunner.class)
public class UrlArgFilterTest {

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

        filter = Filter.create(UrlArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject());

        filters.clear();
        filters.add(filter);
    }

    @Test
    public void testMissParameterAllocateDefaultValue(TestContext testContext) {
        UrlArgPlugin plugin = (UrlArgPlugin) ApiPlugin.create(UrlArgPlugin.class.getSimpleName());
        Parameter parameter = Parameter.create("limit", 10)
                .addRule(Rule.integer())
                .addRule(Rule.max(100))
                .addRule(Rule.min(1));
        plugin.add(parameter);
        parameter = Parameter.create("start", 0)
                .addRule(Rule.integer());
        plugin.add(parameter);
        apiContext.apiDefinition().addPlugin(plugin);
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.assertTrue(context.params().containsKey("limit"));
                    testContext.assertEquals("10", Iterables.get(context.params().get("limit"), 0));
                    testContext.assertEquals("0", Iterables.get(context.params().get("start"), 0));
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testExistParameterNotAllocateDefaultValue(TestContext testContext) {
        UrlArgPlugin plugin = (UrlArgPlugin) ApiPlugin.create(UrlArgPlugin.class.getSimpleName());
        Parameter parameter = Parameter.create("limit", 10)
                .addRule(Rule.integer())
                .addRule(Rule.max(100))
                .addRule(Rule.min(1));
        plugin.add(parameter);
        parameter = Parameter.create("q3", 0);
        plugin.add(parameter);

        apiContext.apiDefinition().addPlugin(plugin);
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.assertTrue(context.params().containsKey("limit"));
                    testContext.assertEquals("10", Iterables.get(context.params().get("limit"), 0));
                    testContext.assertEquals("v3", Iterables.get(context.params().get("q3"), 0));
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }


    @Test
    public void testInvalidParameterShouldThrowValidationException(TestContext testContext) {
        UrlArgPlugin plugin = (UrlArgPlugin) ApiPlugin.create(UrlArgPlugin.class.getSimpleName());
        Parameter parameter = Parameter.create("appKey", null)
                .addRule(Rule.required());
        plugin.add(parameter);
        apiContext.apiDefinition().addPlugin(plugin);
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> async.complete())
                .onFailure(t -> {
                    testContext.assertTrue(t instanceof ValidationException);
                    async.complete();
                });
    }
}
