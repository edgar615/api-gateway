package com.github.edgar615.direvolves.plugin.auth;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class TokenFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  private Vertx vertx;

  private int userId = Integer.parseInt(Randoms.randomNumber(5));

  private String token = UUID.randomUUID().toString();

  private int port = Integer.parseInt(Randoms.randomNumber(4));

  private String path = "/" + UUID.randomUUID().toString();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    filters.clear();

    AtomicBoolean completed = new AtomicBoolean();

    vertx.createHttpServer().requestHandler(req -> {
      if (req.path().equals(path)) {
        req.bodyHandler(body -> {
          String token = body.toJsonObject().getString("token");
          if (token.equalsIgnoreCase(this.token)) {
            JsonObject jsonObject = new JsonObject()
                    .put("userId", userId)
                    .put("username", "edgar615");
            req.response().end(jsonObject.encode());
          } else {
            req.response().setStatusCode(404)
                    .end();
          }
        });
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

  }

  @Test
  public void noHeaderShouldThrowInvalidToken(TestContext testContext) {

    ApiContext apiContext = createApiContext();
    Filter filter = Filter.create(TokenFilter.class.getSimpleName(),
                                  vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, Lists.newArrayList(filter))
            .andThen(context -> testContext.fail())
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.INVALID_REQ, ex.getErrorCode());
              async.complete();
            });
  }

  @Test
  public void lackBearerShouldThrowInvalidToken(TestContext testContext) {
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "invalidtoken"),
                                             ArrayListMultimap.create());

    Filter filter = Filter.create(TokenFilter.class.getSimpleName(),
                                  vertx, new JsonObject());
    filters.add(filter);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, Lists.newArrayList(filter))
            .andThen(context -> testContext.fail())
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());
              async.complete();
            });
  }

  @Test
  public void testAuthSuccess(TestContext testContext) {

    JsonObject userConfig = new JsonObject()
            .put("path", path);

    Filter filter  = Filter.create(TokenFilter.class.getSimpleName(), vertx,
                           new JsonObject().put("token", userConfig)
                                   .put("port", port));
    filters.add(filter);
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject user = context.principal();
              System.out.println(user);
              testContext.assertEquals(userId, user.getValue("userId"));
              testContext.assertTrue(user.containsKey("username"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void testInvalidTokenShouldThrowException(TestContext testContext) {

    JsonObject userConfig = new JsonObject()
            .put("path", path);

    Filter filter  = Filter.create(TokenFilter.class.getSimpleName(), vertx,
                                   new JsonObject().put("token", userConfig)
                                           .put("port", port));
    filters.add(filter);
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + UUID.randomUUID().toString()),
                                             ArrayListMultimap.create());
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> testContext.fail())
            .onFailure(throwable -> {
              throwable.printStackTrace();
              async.complete();
            });
  }

  private ApiContext createApiContext(Multimap<String, String> header,
                                      Multimap<String, String> params) {
    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", header,
                              params, null);
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", 80, "localhost");
    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    TokenPlugin plugin = (TokenPlugin) ApiPlugin.create(TokenPlugin
                                                                                  .class
                                                                                  .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);
    return apiContext;
  }

  private ApiContext createApiContext() {
    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", ArrayListMultimap.create(),
                              ArrayListMultimap.create(), null);
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", 80, "localhost");
    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    TokenPlugin plugin = (TokenPlugin) ApiPlugin.create(TokenPlugin
                                                                                  .class
                                                                                  .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);
    return apiContext;
  }
}
