package com.edgar.direwolves.core.definition;

import com.edgar.direwolves.core.utils.Log;
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

  public static final String MODULE_NAME = "api.discovery";

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
        Log.create(LOGGER)
                .setModule(MODULE_NAME)
                .setEvent("api.add.succeeded")
                .addData("namespace", name)
                .addData("api", definition.name())
                .addData("definition", definition.toJson().encode())
                .info();
        resultHandler.handle(Future.succeededFuture(definition));
      } else {
        Log.create(LOGGER)
                .setModule(MODULE_NAME)
                .setEvent("api.add.failed")
                .addData("namespace", name)
                .addData("api", definition.name())
                .addData("definition", definition.toJson().encode())
                .setThrowable(ar.cause())
                .info();
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
          Log.create(LOGGER)
                  .setModule(MODULE_NAME)
                  .setEvent("api.delete.failed")
                  .addData("namespace", this.name)
                  .addData("api", name)
                  .setThrowable(new NoStackTraceThrowable("Api: '" + name + "' not found"))
                  .info();
          resultHandler.handle(Future.failedFuture("Api: '" + name + "' not found"));
        } else {
          Log.create(LOGGER)
                  .setModule(MODULE_NAME)
                  .setEvent("api.delete.succeeded")
                  .addData("namespace", this.name)
                  .addData("api", name)
                  .addData("definition", ar.result())
                  .info();

          resultHandler.handle(Future.succeededFuture(
                  ApiDefinition.fromJson(new JsonObject(ar.result()))));
        }
      } else {
        Log.create(LOGGER)
                .setModule(MODULE_NAME)
                .setEvent("api.delete.failed")
                .addData("namespace", this.name)
                .addData("api", name)
                .setThrowable(new NoStackTraceThrowable("Api: '" + name + "' not found"))
                .info();
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getDefinitions(Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
    registry.getAll(ar -> {
      if (ar.succeeded()) {
        Log.create(LOGGER)
                .setModule(MODULE_NAME)
                .setEvent("api.getall.succeeded")
                .addData("namespace", this.name)
                .addData("size", ar.result().size())
                .setThrowable(ar.cause())
                .info();

        resultHandler.handle(Future.succeededFuture(ar.result().values().stream()
                                                            .map(s -> ApiDefinition
                                                                    .fromJson(new JsonObject(s)))
                                                            .collect(Collectors.toList())));
      } else {
        Log.create(LOGGER)
                .setModule(MODULE_NAME)
                .setEvent("api.getall.failed")
                .addData("namespace", this.name)
                .setThrowable(ar.cause())
                .info();
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getDefinition(String name, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    registry.get(name, ar -> {
      if (ar.succeeded()) {
        if (ar.result() != null) {
          Log.create(LOGGER)
                  .setModule(MODULE_NAME)
                  .setEvent("api.get.succeeded")
                  .addData("namespace", this.name)
                  .addData("api", name)
                  .addData("definition", ar.result())
                  .setThrowable(ar.cause())
                  .info();

          resultHandler.handle(Future.succeededFuture(
                  ApiDefinition.fromJson(new JsonObject(ar.result()))));
        } else {
          Log.create(LOGGER)
                  .setModule(MODULE_NAME)
                  .setEvent("api.get.failed")
                  .addData("namespace", this.name)
                  .addData("api", name)
                  .setThrowable(new NoStackTraceThrowable("Api: '" + name + "' not found"))
                  .info();
          SystemException ex = SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                  .set("name", name);
          resultHandler.handle(Future.failedFuture(ex));
        }
      } else {
        Log.create(LOGGER)
                .setModule(MODULE_NAME)
                .setEvent("api.get.failed")
                .addData("namespace", this.name)
                .addData("api", name)
                .setThrowable(ar.cause())
                .info();
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }
}
