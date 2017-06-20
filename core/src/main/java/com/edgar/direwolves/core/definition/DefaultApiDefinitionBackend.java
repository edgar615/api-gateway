package com.edgar.direwolves.core.definition;

import com.edgar.util.vertx.sharedata.SyncMap;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
class DefaultApiDefinitionBackend implements ApiDefinitionBackend {
  private final Vertx vertx;

  private SyncMap<String, String> registry;

  public DefaultApiDefinitionBackend(Vertx vertx, String name) {
    this.vertx = vertx;
    this.registry = new SyncMap<>(vertx, name);
  }

  @Override
  public void store(ApiDefinition definition, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    registry.put(definition.name(), definition.toJson().encode(), ar -> {
      if (ar.succeeded()) {
        resultHandler.handle(Future.succeededFuture(definition));
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void remove(String name, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    Objects.requireNonNull(name, "No name");
    registry.remove(name, ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) {
          // Not found
          resultHandler.handle(Future.failedFuture("Api '" + name + "' not found"));
        } else {
          resultHandler.handle(Future.succeededFuture(
                  ApiDefinition.fromJson(new JsonObject(ar.result()))));
        }
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getDefinitions(Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
    registry.getAll(ar -> {
      if (ar.succeeded()) {
        resultHandler.handle(Future.succeededFuture(ar.result().values().stream()
                                                            .map(s -> ApiDefinition
                                                                    .fromJson(new JsonObject(s)))
                                                            .collect(Collectors.toList())));
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getDefinition(String name, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    registry.get(name, ar -> {
      if (ar.succeeded()) {
        if (ar.result() != null) {
          resultHandler.handle(Future.succeededFuture(ApiDefinition.fromJson(new JsonObject(ar.result()))));
        } else {
          resultHandler.handle(Future.succeededFuture(null));
        }
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }
}
