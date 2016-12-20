package com.edgar.direwolves.plugin.authentication;

import com.google.common.collect.ImmutableList;

import com.edgar.direwolves.core.auth.AuthenticationStrategy;
import com.edgar.direwolves.core.auth.AuthenticationStrategyFactory;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public class AuthenticationFilter implements Filter {
  private static final List<AuthenticationStrategyFactory> factories = ImmutableList.copyOf(
          ServiceLoader.load(AuthenticationStrategyFactory.class));

  private final List<AuthenticationStrategy> strategies;

  public AuthenticationFilter(Vertx vertx, JsonObject config) {
    strategies = factories.stream().map(f -> f.create(vertx, config))
            .collect(Collectors.toList());
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    ApiPlugin plugin =
            apiContext.apiDefinition().plugin(AuthenticationPlugin.class.getSimpleName());
    if (plugin == null) {
      return false;
    }
    AuthenticationPlugin authenticationPlugin = (AuthenticationPlugin) plugin;
    return !authenticationPlugin.authentications().isEmpty();
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    AuthenticationPlugin
            plugin =
            (AuthenticationPlugin) apiContext.apiDefinition()
                    .plugin(AuthenticationPlugin.class.getSimpleName());
    List<Future> futures = new ArrayList<>();
    strategies.stream()
            .filter(s -> plugin.authentications().contains(s.name()))
            .forEach(s -> {
              Future<JsonObject> future = Future.future();
              futures.add(future);
              try {
                s.doAuthentication(apiContext, future);
              } catch (Exception e) {
                if (!future.isComplete()) {
                  future.fail(e);
                }
              }
            });
    CompositeFuture.any(futures)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                for (int i = 0; i < futures.size(); i++) {
                  if (futures.get(i).succeeded()) {
                    apiContext.setPrincipal((JsonObject) futures.get(i).result());
                    completeFuture.complete(apiContext);
                    return;
                  }
                }
                completeFuture
                        .fail(SystemException.wrap(DefaultErrorCode.INVALID_TOKEN, ar.cause()));
              } else {
                completeFuture.fail(ar.cause());
              }
            });
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {
  }
}
