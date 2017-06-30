package com.edgar.direwolves.core.definition;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
class ApiDiscoveryImpl implements ApiDiscovery {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiDiscovery.class);
  private final Vertx vertx;

  private final ApiDefinitionBackend backend;

  public ApiDiscoveryImpl(Vertx vertx, String name) {
    this.vertx = vertx;
    this.backend = new DefaultApiDefinitionBackend(vertx, name);
  }

  @Override
  public void publish(ApiDefinition definition, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    backend.store(definition, resultHandler);
//    vertx.eventBus().publish(announce, definition.toJson());
  }

  @Override
  public void unpublish(String name, Handler<AsyncResult<Void>> resultHandler) {
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
    LOGGER.info("---| [api.filtered] [{}]", filter.encode());
    getDefinitions(accept, resultHandler);
  }

  @Override
  public void getDefinitions(Function<ApiDefinition, Boolean> filter,
                             Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
    Objects.requireNonNull(filter);
    backend.getDefinitions(ar -> {
      if (ar.failed()) {
        LOGGER.error("---| [api.filtered]", ar.cause());
        resultHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        List<ApiDefinition> definitions =
                ar.result().stream()
                        .filter(filter::apply)
                        .collect(Collectors.toList());
        LOGGER.info("---| [api.filtered] [{}]", definitions.size());
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
                                                                                  .RESOURCE_NOT_FOUND).set("name", name)));
        } else {
          resultHandler.handle(Future.succeededFuture(definitions.get(0)));
        }
      }
    });
  }

  @Override
  public void close() {
    LOGGER.info("Stopping api discovery");
  }
}
