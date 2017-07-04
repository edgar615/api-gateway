package com.edgar.direwolves.core.definition;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.utils.LoggerUtils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.sharedata.SyncMap;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.NoStackTraceThrowable;
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

  public static final String LOG_EVENT_NAME = "api.discovery";

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
        LoggerUtils.info(LOGGER, LOG_EVENT_NAME, "api.add.succeeded",
                         Lists.newArrayList("namespace", "api", "data"),
                         Lists.newArrayList(name, definition.name(), definition.toJson().encode()));
        resultHandler.handle(Future.succeededFuture(definition));
      } else {
        LoggerUtils.error(LOGGER, LOG_EVENT_NAME, "api.add.failed",
                          Lists.newArrayList("namespace", "api", "data"),
                          Lists.newArrayList(name, definition.name(), definition.toJson().encode()),
                          ar.cause());
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
          LoggerUtils.warn(LOGGER, LOG_EVENT_NAME, "api.delete.failed",
                           Lists.newArrayList("namespace", "api"),
                           Lists.newArrayList(this.name, name),
                           new NoStackTraceThrowable("Api: '" + name + "' not found"));
          resultHandler.handle(Future.failedFuture("Api: '" + name + "' not found"));
        } else {
          LoggerUtils.warn(LOGGER, LOG_EVENT_NAME, "api.delete.succeeded",
                           Lists.newArrayList("namespace", "api", "data"),
                           Lists.newArrayList(this.name, name, ar.result()));
          resultHandler.handle(Future.succeededFuture(
                  ApiDefinition.fromJson(new JsonObject(ar.result()))));
        }
      } else {
        LoggerUtils.error(LOGGER, LOG_EVENT_NAME, "api.delete.failed",
                          Lists.newArrayList("namespace", "api"),
                          Lists.newArrayList(this.name, name),
                          ar.cause());
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getDefinitions(Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
    registry.getAll(ar -> {
      if (ar.succeeded()) {
        LoggerUtils.info(LOGGER, LOG_EVENT_NAME, "api.getall.succeeded",
                         Lists.newArrayList("namespace", "size"),
                         Lists.newArrayList(name, ar.result().size()));
        resultHandler.handle(Future.succeededFuture(ar.result().values().stream()
                                                            .map(s -> ApiDefinition
                                                                    .fromJson(new JsonObject(s)))
                                                            .collect(Collectors.toList())));
      } else {
        LoggerUtils.error(LOGGER, LOG_EVENT_NAME, "api.getall.failed",
                          Lists.newArrayList("namespace"),
                          Lists.newArrayList(name),
                          ar.cause());
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getDefinition(String name, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    registry.get(name, ar -> {
      if (ar.succeeded()) {
        if (ar.result() != null) {
          LoggerUtils.info(LOGGER, LOG_EVENT_NAME, "api.get.succeeded",
                           Lists.newArrayList("namespace", "api", "data"),
                           Lists.newArrayList(this.name, name, ar.result()));
          resultHandler.handle(Future.succeededFuture(
                  ApiDefinition.fromJson(new JsonObject(ar.result()))));
        } else {
          LoggerUtils.warn(LOGGER, LOG_EVENT_NAME, "api.get.failed",
                           Lists.newArrayList("namespace", "api"),
                           Lists.newArrayList(this.name, name),
                           new NoStackTraceThrowable("Api: '" + name + "' not found"));
          SystemException ex = SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                  .set("name", name);
          resultHandler.handle(Future.failedFuture(ex));
        }
      } else {
        LoggerUtils.error(LOGGER, LOG_EVENT_NAME, "api.get.failed",
                          Lists.newArrayList("namespace", "api"),
                          Lists.newArrayList(this.name, name),
                          ar.cause());
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }
}
