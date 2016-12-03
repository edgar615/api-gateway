package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.EventbusUtils;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class JwtCreateFilterTest {

  private final List<Filter> filters = new ArrayList<>();
  JwtCreateFilter filter;

  private Vertx vertx;

  private String userAddAddress = UUID.randomUUID().toString();
  private String userKey = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = new JwtCreateFilter();
    filter.config(vertx, new JsonObject().put("jwt.user.add.address", userAddAddress)
        .put("jwt.user.key", userKey));

    filters.clear();
    filters.add(filter);

    vertx.eventBus().<JsonObject>consumer(userAddAddress, msg -> {
      JsonObject user = msg.body();
      int userId = user.getInteger(userKey);
      if (userId < 10) {
        msg.reply(user);
      } else {
        EventbusUtils.fail(msg, SystemException.create(DefaultErrorCode.INVALID_TOKEN));
      }
    });
  }

  @Test
  public void testErrorJwt(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");
    ApiContext apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    JwtCreatePlugin plugin = (JwtCreatePlugin) ApiPlugin.create(JwtCreatePlugin
        .class
        .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);

    JsonObject body = new JsonObject()
        .put("username", "edgar")
        .put("tel", "123456")
        .put(userKey, 10);
    apiContext.setResponse(new JsonObject()
            .put("body", body)
            .put("statusCode", 200)
    );


    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> testContext.fail())
        .onFailure(throwable -> {
          async.complete();
        });
  }

  @Test
  public void testCreateJwt(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    ApiContext apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    JsonObject body = new JsonObject()
        .put("username", "edgar")
        .put("tel", "123456")
        .put(userKey, 1);
    apiContext.setResponse(new JsonObject()
            .put("body", body)
            .put("statusCode", 200)
    );

    JwtCreatePlugin plugin = (JwtCreatePlugin) ApiPlugin.create(JwtCreatePlugin
        .class
        .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> {
          JsonObject response = context.response();
          String token = response.getJsonObject("body").getString("token");
          String token2 = Iterables.get(Splitter.on(".").split(token), 1);
          JsonObject chaim = new JsonObject(new String(Base64.getDecoder().decode(token2)));
          System.out.println(chaim);
          System.out.println(new Date(chaim.getLong("exp") * 1000));
          testContext.assertTrue(chaim.containsKey("jti"));
          async.complete();
        })
        .onFailure(throwable -> {
          throwable.printStackTrace();
          testContext.fail();
        });
  }

}
