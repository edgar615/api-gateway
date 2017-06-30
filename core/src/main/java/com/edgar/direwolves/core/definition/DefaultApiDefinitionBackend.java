package com.edgar.direwolves.core.definition;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.sharedata.SyncMap;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
class DefaultApiDefinitionBackend implements ApiDefinitionBackend {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiDefinitionBackend.class);

  private final Vertx vertx;

  private final SyncMap<String, String> registry;

  private final String name;

  public DefaultApiDefinitionBackend(Vertx vertx, String name) {
    this.vertx = vertx;
    this.registry = new SyncMap<>(vertx, name);
    this.name = name;
  }

  @Override
  public void store(ApiDefinition definition, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    Objects.requireNonNull(definition, "definition is null");
    registry.put(definition.name(), definition.toJson().encode(), ar -> {
      if (ar.succeeded()) {
        LOGGER.info("[api.added] [{}] [{}]", definition.name(),
                    definition.toJson().encode());
        resultHandler.handle(Future.succeededFuture(definition));
      } else {
        LOGGER.error("[api.added] [{}] [{}]", definition.name(),
                     definition.toJson().encode(), ar.cause());
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void remove(String name, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    Objects.requireNonNull(name, "name required");
    registry.remove(name, ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) {
          // Not found
          LOGGER.warn("[api.deleted] [{}] [not found]", name);
          resultHandler.handle(Future.failedFuture("Api: '" + name + "' not found"));
        } else {
          LOGGER.info("[api.deleted] [{}] [{}]", name, ar.result());
          resultHandler.handle(Future.succeededFuture(
                  ApiDefinition.fromJson(new JsonObject(ar.result()))));
        }
      } else {
        LOGGER.error("[api.deleted] [{}]", name, ar.cause());
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getDefinitions(Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
    registry.getAll(ar -> {
      if (ar.succeeded()) {
        LOGGER.info("[api.all] [{}]", ar.result().size());
        resultHandler.handle(Future.succeededFuture(ar.result().values().stream()
                                                            .map(s -> ApiDefinition
                                                                    .fromJson(new JsonObject(s)))
                                                            .collect(Collectors.toList())));
      } else {
        LOGGER.error("[api.all]", name, ar.cause());
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getDefinition(String name, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    registry.get(name, ar -> {
      if (ar.succeeded()) {
        if (ar.result() != null) {
          LOGGER.info("[ApiFinded] [{}] [{}]", name, ar.result());
          resultHandler.handle(Future.succeededFuture(
                  ApiDefinition.fromJson(new JsonObject(ar.result()))));
        } else {
          LOGGER.warn("[ApiFinded] [{}] [not found]", name);
          SystemException ex = SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                  .set("name", name);
          resultHandler.handle(Future.failedFuture(ex));
        }
      } else {
        LOGGER.error("[ApiFinded] [{}]", name, ar.cause());
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }
}
