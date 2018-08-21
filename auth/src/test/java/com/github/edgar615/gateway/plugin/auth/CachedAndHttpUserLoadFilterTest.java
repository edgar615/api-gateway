package com.github.edgar615.gateway.plugin.auth;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.utils.CacheUtils;
import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheOptions;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class CachedAndHttpUserLoadFilterTest {

    private final List<Filter> filters = new ArrayList<>();

    Filter filter;

    String id = UUID.randomUUID().toString();

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        filters.clear();
    }

    @Test
    public void testNoUser(TestContext testContext) {
        int port = Integer.parseInt(Randoms.randomNumber(4));
        String path = Randoms.randomAlphabet(10);
        AtomicInteger reqCount = mockExistHttp(port, path);
        mockCache();
        JsonObject config = new JsonObject()
                .put("cacheEnable", true)
                .put("api", path);
        filter = Filter.create(UserLoaderFilter.class.getSimpleName(), vertx,
                               new JsonObject().put("user", config)
                                       .put("port", port));
        filters.add(filter);
        ApiContext apiContext = createContext();

        JsonObject body = new JsonObject()
                .put("userId", 10);
        apiContext.setPrincipal(body);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        AtomicBoolean check1 = new AtomicBoolean();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    JsonObject user = context.principal();
                    System.out.println(user);
                    testContext.assertFalse(user.containsKey("username"));
                    testContext.fail();
                })
                .onFailure(e -> {
                    testContext.assertTrue(e instanceof SystemException);
                    SystemException ex = (SystemException) e;
                    testContext.assertEquals(DefaultErrorCode.UNKOWN_ACCOUNT.getNumber(),
                                             ex.getErrorCode().getNumber());

                    check1.set(true);
                });
        Awaitility.await().until(() -> check1.get());

        task = Task.create();
        task.complete(apiContext);
        AtomicBoolean check2 = new AtomicBoolean();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    JsonObject user = context.principal();
                    System.out.println(user);
                    testContext.assertFalse(user.containsKey("username"));
                    testContext.fail();
                })
                .onFailure(e -> {
                    testContext.assertTrue(e instanceof SystemException);
                    SystemException ex = (SystemException) e;
                    testContext.assertEquals(DefaultErrorCode.UNKOWN_ACCOUNT.getNumber(),
                                             ex.getErrorCode().getNumber());

                    check2.set(true);
                });
        Awaitility.await().until(() -> check2.get());
        testContext.assertEquals(1, reqCount.get());
    }

    @Test
    public void testLoadSuccess(TestContext testContext) {
        int port = Integer.parseInt(Randoms.randomNumber(4));
        String path = Randoms.randomAlphabet(10);
        AtomicInteger reqCount = mockExistHttp(port, path);
        mockCache();
        JsonObject config = new JsonObject()
                .put("cacheEnable", true)
                .put("api", path);
        filter = Filter.create(UserLoaderFilter.class.getSimpleName(), vertx,
                               new JsonObject().put("user", config)
                                       .put("port", port));
        filters.add(filter);
        ApiContext apiContext = createContext();

        JsonObject body = new JsonObject()
                .put("userId", id);
        apiContext.setPrincipal(body);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        AtomicBoolean check1 = new AtomicBoolean();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    JsonObject user = context.principal();
                    System.out.println(user);
                    testContext.assertTrue(user.containsKey("username"));
                    check1.set(true);
                })
                .onFailure(e -> {
                    e.printStackTrace();
                    testContext.fail();
                });
        Awaitility.await().until(() -> check1.get());

        AtomicBoolean check2 = new AtomicBoolean();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    JsonObject user = context.principal();
                    System.out.println(user);
                    testContext.assertTrue(user.containsKey("username"));
                    check2.set(true);
                })
                .onFailure(e -> {
                    e.printStackTrace();
                    testContext.fail();
                });
        Awaitility.await().until(() -> check2.get());
        testContext.assertEquals(1, reqCount.get());

    }

    private ApiContext createContext() {
        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("q3", "v3");
        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put("h3", "v3");
        headers.put("h3", "v3.2");
        ApiContext apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
        SimpleHttpEndpoint httpEndpoint =
                SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", 80, "localhost");
        ApiDefinition definition =
                ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists
                        .newArrayList(httpEndpoint));
        apiContext.setApiDefinition(definition);
        UserLoaderPlugin plugin =
                (UserLoaderPlugin) ApiPlugin.create(UserLoaderPlugin.class.getSimpleName());
        apiContext.apiDefinition().addPlugin(plugin);
        return apiContext;
    }

    private Cache<String, JsonObject> mockCache() {
        Cache<String, JsonObject> cache = CacheUtils.createCache(vertx, "user", new CacheOptions());
        return cache;
    }

    private AtomicInteger mockExistHttp(int port, String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        AtomicInteger reqCount = new AtomicInteger();
        AtomicBoolean completed = new AtomicBoolean();
        vertx.createHttpServer().requestHandler(req -> {
            String userId = req.getParam("userId");
            reqCount.incrementAndGet();
            if (id.equalsIgnoreCase(userId)) {
                JsonObject jsonObject = new JsonObject()
                        .put("userId", userId)
                        .put("username", "edgar615");
                req.response().end(jsonObject.encode());
            } else {
                req.response().setStatusCode(404)
                        .end();
            }

        }).listen(port, ar -> {
            if (ar.succeeded()) {
                completed.set(true);
            } else {
                ar.cause().printStackTrace();
            }
        });

        Awaitility.await().until(() -> completed.get());
        return reqCount;
    }


}
