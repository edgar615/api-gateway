package com.github.edgar615.gateway.core.apidiscovery;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.vertx.sharedata.SyncMap;
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
 * 单机使用同步map,机器使用异步map，性能不好.
 * 实际上在使用中很少从集群中读取API，最终还是要做一个本地缓存,所以不再使用这种方式，自己使用本地内存的方式.
 *
 * @author Edgar  Date 2017/6/20
 */
@Deprecated
class SyncApiDefinitionBackend implements ApiDefinitionBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiDefinitionBackend.class);

  private final Vertx vertx;

  private final SyncMap<String, String> registry;

  private final String name;

  public SyncApiDefinitionBackend(Vertx vertx, String name) {
    this.vertx = vertx;
    this.registry = new SyncMap<>(vertx, name);
    this.name = name;
  }

  @Override
  public void store(ApiDefinition definition, Handler<AsyncResult<ApiDefinition>> resultHandler) {
    Objects.requireNonNull(definition, "definition is null");
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
    Objects.requireNonNull(name, "name required");
    registry.remove(name, ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) {
          // Not found
          resultHandler.handle(Future.failedFuture("Api: '" + name + "' not found"));
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
          resultHandler.handle(Future.succeededFuture(
                  ApiDefinition.fromJson(new JsonObject(ar.result()))));
        } else {
          SystemException ex = SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                  .set("name", name);
          resultHandler.handle(Future.failedFuture(ex));
        }
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> resultHandler) {
    //unSupport
  }
}
