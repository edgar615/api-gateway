package com.github.edgar615.direwolves.core.apidiscovery;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public ApiDiscoveryImpl(Vertx vertx, ApiDiscoveryOptions options) {
    Objects.requireNonNull(options.getName());
    Objects.requireNonNull(options.getPublishedAddress());
    Objects.requireNonNull(options.getUnpublishedAddress());
    this.vertx = vertx;
    this.options = options;
    this.name = options.getName();
    this.backend = new DefaultApiDefinitionBackend(vertx, name);
    this.publishedAddress = this.name + "." + options.getPublishedAddress();
    this.unpublishedAddress = this.name + "." + options.getUnpublishedAddress();
    Log.create(LOGGER)
            .setEvent("api.discovery.start")
            .addData("namespace", this.name)
            .info();
  }

  @Override
  public void publish(ApiDefinition definition, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    Log.create(LOGGER)
            .setEvent("api.publish")
            .addData("namespace", this.name)
            .addData("definition", definition.toJson().encode())
            .info();
    backend.store(definition, ar -> {
      if (ar.succeeded()) {
        vertx.eventBus().publish(publishedAddress, definition.toJson());
      }
      resultHandler.handle(ar);
    });
  }

  @Override
  public void unpublish(String name, Handler<AsyncResult<Void>> resultHandler) {
    Log.create(LOGGER)
            .setEvent("api.unpublish")
            .addData("namespace", this.name)
            .addData("name", name)
            .info();
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
    Log.create(LOGGER)
            .setEvent("api.filter")
            .addData("namespace", this.name)
            .addData("filter", filter)
            .info();

    getDefinitions(accept, resultHandler);
  }

  @Override
  public void getDefinitions(Function<ApiDefinition, Boolean> filter,
                             Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
    Objects.requireNonNull(filter);
    backend.getDefinitions(ar -> {
      if (ar.failed()) {
        Log.create(LOGGER)
                .setEvent("api.filter")
                .addData("namespace", this.name)
                .setThrowable(ar.cause())
                .error();
        resultHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        List<ApiDefinition> definitions =
                ar.result().stream()
                        .filter(filter::apply)
                        .collect(Collectors.toList());
        Log.create(LOGGER)
                .setEvent("api.filter")
                .addData("namespace", this.name)
                .addData("size", definitions.size())
                .info();
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
                Log.create(LOGGER)
                        .setEvent("api.importer.started")
                        .addData("namespace", this.name)
                        .addData("importer", importer)
                        .setMessage("Cannot start the api importer")
                        .setThrowable(ar.cause())
                        .error();
                if (completionHandler != null) {
                  completionHandler.handle(Future.failedFuture(ar.cause()));
                }
              } else {
                importers.add(importer);
                Log.create(LOGGER)
                        .setEvent("api.importer.started")
                        .addData("namespace", this.name)
                        .addData("importer", importer)
                        .setMessage("Api importer started")
                        .info();
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
    Log.create(LOGGER)
            .setEvent("api.discovery.close")
            .addData("namespace", this.name)
            .info();
  }
}
