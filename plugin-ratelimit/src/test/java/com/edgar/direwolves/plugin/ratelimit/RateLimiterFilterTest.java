package com.edgar.direwolves.plugin.ratelimit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.direwolves.redis.RedisProviderFactory;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/6/23.
 *
 * @author Edgar  Date 2017/6/23
 */
@RunWith(VertxUnitRunner.class)
public class RateLimiterFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Vertx vertx;

  Filter filter;


  private String namespace = UUID.randomUUID().toString();

  private String cacheAddress = namespace + "." + RedisProvider.class.getName();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    JsonObject config = new JsonObject()
            .put("redis.host", "10.11.0.31");
    RedisProvider redis = new RedisProviderFactory().create(vertx, config);
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ProxyHelper.registerService(RedisProvider.class, vertx, redis,
                                cacheAddress);
  }

  @Test
  public void testBurst3Refill1In2000(TestContext testContext) {
    String subject = UUID.randomUUID().toString();
    String name = UUID.randomUUID().toString();
    JsonObject config = new JsonObject()
            .put(name, new JsonObject().put("key", subject)
                    .put("limit", 1)
                    .put("interval", 2)
                    .put("unit", TimeUnit.SECONDS.name()));
    filter = new RateLimiterFilterFactory().create(vertx, new JsonObject()
            .put("rate.limiter", config)
            .put("namespace", namespace));
    filters.add(filter);

    ApiContext apiContext = createContext();
    RateLimiterPlugin plugin = RateLimiterPlugin.create();
    plugin.addRateLimiter(new RateLimiter(name, 3));
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    AtomicBoolean check1 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.variables());
              testContext.assertEquals(2l, context.variables()
                      .get("resp.header:X-Rate-Limit-Remaining"));
              check1.set(true);
            }).onFailure(t -> {
      t.printStackTrace();
      Assert.fail();
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check2 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.variables());
              testContext.assertEquals(1l, context.variables()
                      .get("resp.header:X-Rate-Limit-Remaining"));
              check2.set(true);
            }).onFailure(t -> {
      t.printStackTrace();
      Assert.fail();
    });
    Awaitility.await().until(() -> check2.get());

    AtomicBoolean check3 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.variables());
              testContext.assertEquals(0l, context.variables()
                      .get("resp.header:X-Rate-Limit-Remaining"));
              check3.set(true);
            }).onFailure(t -> {
      t.printStackTrace();
      Assert.fail();
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check4 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Assert.fail();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.assertTrue(t instanceof SystemException);
      Map<String, Object> map =( (SystemException) t).getProperties();
      testContext.assertEquals(0l, map
              .get("resp.header:X-Rate-Limit-Remaining"));
      check4.set(true);
    });
    Awaitility.await().until(() -> check4.get());

  }

  @Test
  public void testBurst3AndBurst5Refill1In2000(TestContext testContext) {
    String subject = UUID.randomUUID().toString();
    String name = UUID.randomUUID().toString();
    String name2 = UUID.randomUUID().toString();
    JsonObject config = new JsonObject()
            .put(name, new JsonObject().put("key", subject)
                    .put("limit", 1)
                    .put("interval", 2)
                    .put("unit", TimeUnit.SECONDS.name()));
    filter = new RateLimiterFilterFactory().create(vertx, new JsonObject()
            .put("rate.limiter", config)
            .put("namespace", namespace));
    filters.add(filter);

    ApiContext apiContext = createContext();
    RateLimiterPlugin plugin = RateLimiterPlugin.create();
    plugin.addRateLimiter(new RateLimiter(name, 3));
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    AtomicBoolean check1 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.variables());
              testContext.assertEquals(2l, context.variables()
                      .get("resp.header:X-Rate-Limit-Remaining"));
              check1.set(true);
            }).onFailure(t -> {
      t.printStackTrace();
      Assert.fail();
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check2 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.variables());
              testContext.assertEquals(1l, context.variables()
                      .get("resp.header:X-Rate-Limit-Remaining"));
              check2.set(true);
            }).onFailure(t -> {
      t.printStackTrace();
      Assert.fail();
    });
    Awaitility.await().until(() -> check2.get());

    AtomicBoolean check3 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.variables());
              testContext.assertEquals(0l, context.variables()
                      .get("resp.header:X-Rate-Limit-Remaining"));
              check3.set(true);
            }).onFailure(t -> {
      t.printStackTrace();
      Assert.fail();
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check4 = new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Assert.fail();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.assertTrue(t instanceof SystemException);
      Map<String, Object> map =( (SystemException) t).getProperties();
      testContext.assertEquals(0l, map
              .get("resp.header:X-Rate-Limit-Remaining"));
      check4.set(true);
    });
    Awaitility.await().until(() -> check4.get());

    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    plugin = RateLimiterPlugin.create();
    plugin.addRateLimiter(new RateLimiter(name, 5));
    apiContext.apiDefinition().addPlugin(plugin);

    AtomicBoolean check5= new AtomicBoolean();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Assert.fail();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.assertTrue(t instanceof SystemException);
      Map<String, Object> map =( (SystemException) t).getProperties();
      testContext.assertEquals(0l, map
              .get("resp.header:X-Rate-Limit-Remaining"));
      check5.set(true);
    });
    Awaitility.await().until(() -> check5.get());
  }

  private ApiContext createContext() {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");
    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
            HttpEndpoint.http("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists
            .newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    return apiContext;
  }

}
