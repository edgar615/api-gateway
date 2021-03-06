package com.github.edgar615.gateway.plugin.appkey;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by edgar on 16-10-31.
 */
@RunWith(VertxUnitRunner.class)
public class CacheAndHttpAppKeyFilterTest extends AbstractAppKeyFilterTest {

    private final List<Filter> filters = new ArrayList<>();

    String appKey = UUID.randomUUID().toString();

    String appSecret = UUID.randomUUID().toString();

    int appId = Integer.parseInt(Randoms.randomNumber(3));

    String signMethod = "HMACMD5";

    private Filter filter;

    private ApiContext apiContext;

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        filters.clear();

    }

    @After
    public void tearDown(TestContext testContext) {
        vertx.close();
    }

    @Test
    public void undefinedAppKeyShouldThrowInvalidReq(TestContext testContext) {
        Cache<String, JsonObject> cache = mockCache();
        int port = Integer.parseInt(Randoms.randomNumber(4));
        String path = Randoms.randomAlphabet(10);
        AtomicInteger reqCount = mockExistHttp(port, path);

        JsonObject config = new JsonObject()
                .put("cacheEnable", true)
                .put("api", path);
        filters.clear();
        filter = Filter.create(AppKeyFilter.class.getSimpleName(), vertx, new JsonObject()
                .put("appkey", config)
                .put("port", port));
        filters.add(filter);
        apiContext = createContext(UUID.randomUUID().toString(), signMethod);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        AtomicBoolean check1 = new AtomicBoolean();
        Filters.doFilter(task, filters)
                .andThen(context -> testContext.fail())
                .onFailure(t -> {
                    testContext.assertTrue(t instanceof SystemException);
                    SystemException ex = (SystemException) t;
                    testContext.assertEquals(DefaultErrorCode.INVALID_REQ, ex.getErrorCode());
                    check1.set(true);
                });
        Awaitility.await().until(() -> check1.get());

        task = Task.create();
        task.complete(apiContext);
        AtomicBoolean check2 = new AtomicBoolean();
        Filters.doFilter(task, filters)
                .andThen(context -> testContext.fail())
                .onFailure(t -> {
                    testContext.assertTrue(t instanceof SystemException);
                    SystemException ex = (SystemException) t;
                    testContext.assertEquals(DefaultErrorCode.INVALID_REQ, ex.getErrorCode());
                    check2.set(true);
                });
        Awaitility.await().until(() -> check2.get());
        testContext.assertEquals(1, reqCount.get());
    }


    @Test
    public void testAppKeyFromHttp(TestContext testContext) {
        Cache<String, JsonObject> cache = mockCache();
        int port = Integer.parseInt(Randoms.randomNumber(4));
        String path = Randoms.randomAlphabet(10);
        AtomicInteger reqCount = mockExistHttp(port, path);

        JsonObject config = new JsonObject()
                .put("cacheEnable", true)
                .put("api", path);
        filters.clear();
        filter = Filter.create(AppKeyFilter.class.getSimpleName(), vertx, new JsonObject()
                .put("appkey", config)
                .put("port", port));
        filters.add(filter);
        apiContext = createContext(UUID.randomUUID().toString(), signMethod);

        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("appKey", appKey);
        params.put("nonce", Randoms.randomAlphabetAndNum(10));
        params.put("signMethod", signMethod);
        params.put("v", "1.0");
        params.put("deviceId", "1");

        params.put("sign", signTopRequest(params, appSecret, signMethod));
        params.removeAll("body");

        ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);


        SimpleHttpEndpoint httpEndpoint =
                SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                        80, "localhost");
        ApiDefinition definition = ApiDefinition
                .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
        apiContext.setApiDefinition(definition);
        definition.addPlugin(ApiPlugin.create(AppKeyPlugin.class.getSimpleName()));

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        AtomicBoolean check1 = new AtomicBoolean();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.assertTrue(context.params().containsKey("sign"));
                    testContext.assertTrue(context.params().containsKey("signMethod"));
                    testContext.assertTrue(context.params().containsKey("v"));
                    testContext.assertTrue(context.params().containsKey("appKey"));
                    check1.set(true);
                })
                .onFailure(t -> {
                    t.printStackTrace();
                    testContext.fail();
                });
        Awaitility.await().until(() -> check1.get());

        task = Task.create();
        task.complete(apiContext);
        AtomicBoolean check2 = new AtomicBoolean();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.assertTrue(context.params().containsKey("sign"));
                    testContext.assertTrue(context.params().containsKey("signMethod"));
                    testContext.assertTrue(context.params().containsKey("v"));
                    testContext.assertTrue(context.params().containsKey("appKey"));
                    check2.set(true);
                })
                .onFailure(t -> {
                    t.printStackTrace();
                    testContext.fail();
                });
        Awaitility.await().until(() -> check2.get());
        testContext.assertEquals(1, reqCount.get());
    }


    @Test
    public void testAppKeyFromCache(TestContext testContext) {
        Cache<String, JsonObject> cache = mockCache();
        AtomicBoolean complete = new AtomicBoolean();
        JsonObject jsonObject = new JsonObject()
                .put("appKey", appKey)
                .put("appSecret", appSecret)
                .put("appId", appId)
                .put("permissions", "all");
        cache.put("appKey:" + appKey, jsonObject, ar -> {
            complete.set(true);
        });
        Awaitility.await().until(() -> complete.get());

        int port = Integer.parseInt(Randoms.randomNumber(4));
        String path = Randoms.randomAlphabet(10);
        AtomicInteger reqCount = mockExistHttp(port, path);

        JsonObject config = new JsonObject()
                .put("cacheEnable", true)
                .put("api", path);
        filters.clear();
        filter = Filter.create(AppKeyFilter.class.getSimpleName(), vertx, new JsonObject()
                .put("appkey", config)
                .put("port", port));
        filters.add(filter);
        apiContext = createContext(UUID.randomUUID().toString(), signMethod);

        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("appKey", appKey);
        params.put("nonce", Randoms.randomAlphabetAndNum(10));
        params.put("signMethod", signMethod);
        params.put("v", "1.0");
        params.put("deviceId", "1");

        params.put("sign", signTopRequest(params, appSecret, signMethod));
        params.removeAll("body");

        ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);


        SimpleHttpEndpoint httpEndpoint =
                SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                        80, "localhost");
        ApiDefinition definition = ApiDefinition
                .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
        apiContext.setApiDefinition(definition);
        definition.addPlugin(ApiPlugin.create(AppKeyPlugin.class.getSimpleName()));

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        AtomicBoolean check1 = new AtomicBoolean();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.assertTrue(context.params().containsKey("sign"));
                    testContext.assertTrue(context.params().containsKey("signMethod"));
                    testContext.assertTrue(context.params().containsKey("v"));
                    testContext.assertTrue(context.params().containsKey("appKey"));
                    check1.set(true);
                })
                .onFailure(t -> {
                    t.printStackTrace();
                    testContext.fail();
                });
        Awaitility.await().until(() -> check1.get());
        testContext.assertEquals(0, reqCount.get());
    }


    private Cache<String, JsonObject> mockCache() {
        Cache<String, JsonObject> cache =
                CacheUtils.createCache(vertx, "appKey", new CacheOptions());
        return cache;
    }

    private AtomicInteger mockExistHttp(int port, String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        AtomicInteger reqCount = new AtomicInteger();
        AtomicBoolean complete = new AtomicBoolean();
        final String finalPath = path;
        vertx.createHttpServer().requestHandler(req -> {
            reqCount.incrementAndGet();
            if (req.path().equals(finalPath)) {
                JsonObject jsonObject = new JsonObject()
                        .put("appKey", appKey)
                        .put("appSecret", appSecret)
                        .put("appId", appId)
                        .put("permissions", "all");
                req.response().end(jsonObject.encode());
            } else {
                req.response().setStatusCode(404).end();
            }
        }).listen(port, ar -> {
            if (ar.succeeded()) {
                complete.set(true);
            } else {

            }
        });

        Awaitility.await().until(() -> complete.get());
        return reqCount;
    }

}
