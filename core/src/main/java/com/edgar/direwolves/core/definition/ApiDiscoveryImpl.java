package com.edgar.direwolves.core.definition;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.utils.LoggerUtils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
class ApiDiscoveryImpl implements ApiDiscovery {

  public static final String LOG_EVENT_NAME = "api.discovery";

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiDiscovery.class);

  private final Vertx vertx;

  private final ApiDefinitionBackend backend;

  private final String name;

  public ApiDiscoveryImpl(Vertx vertx, String name) {
    this.vertx = vertx;
    this.name = name;
    this.backend = new DefaultApiDefinitionBackend(vertx, name);
    LoggerUtils.info(LOGGER, LOG_EVENT_NAME, "start",
                     Lists.newArrayList("namespace"),
                     Lists.newArrayList(this.name));
  }

  @Override
  public void publish(ApiDefinition definition, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    LoggerUtils.info(LOGGER, LOG_EVENT_NAME, "api.publish",
                     Lists.newArrayList("namespace", "definition"),
                     Lists.newArrayList(this.name, definition.toJson().encode()));
    backend.store(definition, resultHandler);
//    vertx.eventBus().publish(announce, definition.toJson());
  }

  @Override
  public void unpublish(String name, Handler<AsyncResult<Void>> resultHandler) {
    LoggerUtils.info(LOGGER, LOG_EVENT_NAME, "api.unpublish",
                     Lists.newArrayList("namespace", "name"),
                     Lists.newArrayList(this.name, name));
    backend.remove(name, ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
        return;
      }
//      ApiDefinition definition = ar.result();
//      if (definition != null) {
//        vertx.eventBus().publish(announce, definition.toJson());
//      }
      resultHandler.handle(Future.succeededFuture());
    });

  }

  @Override
  public void getDefinitions(JsonObject filter,
                             Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
    Objects.requireNonNull(filter);
    Function<ApiDefinition, Boolean> accept;
    if (filter == null) {
      accept = r -> true;
    } else {
      accept = r -> r.match(filter);
    }
    LoggerUtils.info(LOGGER, LOG_EVENT_NAME, "filter",
                     Lists.newArrayList("namespace", "filter"),
                     Lists.newArrayList(this.name, filter.encode()));
    getDefinitions(accept, resultHandler);
  }

  @Override
  public void getDefinitions(Function<ApiDefinition, Boolean> filter,
                             Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
    Objects.requireNonNull(filter);
    backend.getDefinitions(ar -> {
      if (ar.failed()) {
        LoggerUtils.error(LOGGER, LOG_EVENT_NAME, "filter",
                          Lists.newArrayList("namespace"),
                          Lists.newArrayList(this.name)
                , ar.cause());
        resultHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        List<ApiDefinition> definitions =
                ar.result().stream()
                        .filter(filter::apply)
                        .collect(Collectors.toList());
        LoggerUtils.info(LOGGER, LOG_EVENT_NAME, "filter",
                         Lists.newArrayList("namespace", "size"),
                         Lists.newArrayList(this.name, definitions.size()));
        resultHandler.handle(Future.succeededFuture(definitions));
      }
    });
  }

  @Override
  public void getDefinition(String name,
                            Handler<AsyncResult<ApiDefinition>> resultHandler) {
    Objects.requireNonNull(name);
    getDefinitions(d -> d.name().equalsIgnoreCase(name), ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        List<ApiDefinition> definitions = ar.result();
        if (definitions.isEmpty()) {
          resultHandler.handle(Future.failedFuture(SystemException.create(DefaultErrorCode
                                                                                  .RESOURCE_NOT_FOUND)
                                                           .set("name", name)));
        } else {
          resultHandler.handle(Future.succeededFuture(definitions.get(0)));
        }
      }
    });
  }

  @Override
  public void close() {
    LoggerUtils.info(LOGGER, LOG_EVENT_NAME, "close",
                     Lists.newArrayList("namespace"),
                     Lists.newArrayList(this.name));
  }
}
