package com.github.edgar615.direwolves.core.apidiscovery;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
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

  private final String name;

  private final String publishedAddress;

  private final String unpublishedAddress;

  private final Set<ApiImporter> importers = new CopyOnWriteArraySet<>();

  private final ApiDiscoveryOptions options;

  ApiDiscoveryImpl(Vertx vertx, ApiDiscoveryOptions options) {
    Objects.requireNonNull(options.getName());
    Objects.requireNonNull(options.getPublishedAddress());
    Objects.requireNonNull(options.getUnpublishedAddress());
    this.vertx = vertx;
    this.options = options;
    this.name = options.getName();
    this.backend = new DefaultApiDefinitionBackend(vertx, name);
    this.publishedAddress = this.name + "." + options.getPublishedAddress();
    this.unpublishedAddress = this.name + "." + options.getUnpublishedAddress();
  }

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public void publish(ApiDefinition definition, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    LOGGER.info("[ApiDiscovery] [publish api] {}, {}, {}", this.name, definition.name(),
                definition.toJson().encode());
    backend.store(definition, ar -> {
      if (ar.succeeded()) {
        vertx.eventBus().publish(publishedAddress, definition.toJson());
      }
      resultHandler.handle(ar);
    });
  }

  @Override
  public void unpublish(String name, Handler<AsyncResult<Void>> resultHandler) {
    LOGGER.info("[ApiDiscovery,{}] [unpublish api] {}", this.name, name);
    backend.remove(name, ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      ApiDefinition definition = ar.result();
      if (definition != null) {
        vertx.eventBus().publish(unpublishedAddress, definition.toJson());
      }
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
    getDefinitions(accept, resultHandler);
  }

  @Override
  public void getDefinitions(Function<ApiDefinition, Boolean> filter,
                             Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
    Objects.requireNonNull(filter);
    backend.getDefinitions(ar -> {
      if (ar.failed()) {
        LOGGER.error("[ApiDiscovery,{}] [filter api] {}", this.name, filter, ar.cause());
        resultHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        List<ApiDefinition> definitions =
                ar.result().stream()
                        .filter(filter::apply)
                        .collect(Collectors.toList());
        LOGGER.debug("[ApiDiscovery,{}] [filter api] {}, {}", this.name, filter,
                     definitions.size());
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
  public ApiDiscoveryOptions options() {
    return options;
  }

  @Override
  public ApiDiscovery registerImporter(ApiImporter importer, JsonObject config,
                                       Handler<AsyncResult<Void>> completionHandler) {
    JsonObject conf;
    if (config == null) {
      conf = new JsonObject();
    } else {
      conf = config;
    }

    Future<Void> completed = Future.future();
    completed.setHandler(
            ar -> {
              if (ar.failed()) {
                LOGGER.error("[ApiDiscovery,{}] [start import]", this
                        .name, ar.cause());
                if (completionHandler != null) {
                  completionHandler.handle(Future.failedFuture(ar.cause()));
                }
              } else {
                importers.add(importer);
                LOGGER.info("[ApiDiscovery,{}] [start import]", this.name);
                if (completionHandler != null) {
                  completionHandler.handle(Future.succeededFuture(null));
                }
              }
            }
    );

    importer.start(vertx, this, conf, completed);
    return this;
  }

  @Override
  public void close() {
    LOGGER.info("[ApiDiscovery,{}] [close]", this.name);

  }

  public void reload() {
    clear(ar -> {

    });
  }

  private void clear(Handler<AsyncResult<Void>> completionHandler) {
    List<Future> futures = new ArrayList<>();
    getDefinitions(d -> true, ar -> {
      if (ar.failed()) {
        return;
      }
      ar.result().forEach(d -> {
        Future<Void> future = Future.future();
        futures.add(future);
        unpublish(d.name(), future.completer());
      });
    });
    CompositeFuture.all(futures)
            .setHandler(ar -> {
              completionHandler.handle(Future.succeededFuture());
            });
  }

}
