package com.github.edgar615.direwolves.core.apidiscovery;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.utils.Log;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 因为ApiDiscovery的Backend多少会有一些性能损耗，所以在每个网关模块内部使用一个本地缓存保存API定义.
 *
 * @author Edgar  Date 2017/7/13
 */
class ApiFinderImpl implements ApiFinder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiFinder.class);

  private final List<ApiDefinition> definitions = new CopyOnWriteArrayList<>();

  private ApiDiscovery discovery;

  ApiFinderImpl(Vertx vertx, ApiDiscovery discovery) {
    this.discovery = discovery;
    reload("*", ar -> {
      if (ar.succeeded()) {
        Log.create(LOGGER)
                .setEvent("cache.load.succeed");
      } else {
        Log.create(LOGGER)
                .setEvent("cache.load.failure")
                .setThrowable(ar.cause());
      }
    });

    String publishedAddress = discovery.options().getName() + "." + discovery.options().getPublishedAddress();
    String unpublishedAddress = discovery.options().getName() + "." + discovery.options().getUnpublishedAddress();
    vertx.eventBus().<JsonObject>consumer(publishedAddress, msg -> {
      ApiDefinition apiDefinition = ApiDefinition.fromJson(msg.body());
      definitions.removeIf(d -> d.name().equalsIgnoreCase(apiDefinition.name()));
      definitions.add(apiDefinition);
    });

    vertx.eventBus().<JsonObject>consumer(unpublishedAddress, msg -> {
      ApiDefinition apiDefinition = ApiDefinition.fromJson(msg.body());
      definitions.removeIf(d -> d.name().equalsIgnoreCase(apiDefinition.name()));
    });

  }

  @Override
  public void getDefinitions(String method, String path,
                             Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
    Objects.requireNonNull(method);
    Objects.requireNonNull(path);
    JsonObject filter = new JsonObject()
            .put("method", method)
            .put("path", path);
    List<ApiDefinition> list = definitions
            .stream()
            .filter(d -> d.match(filter))
            .collect(Collectors.toList());
    resultHandler.handle(Future.succeededFuture(list));
  }

  @Override
  public int size() {
    return definitions.size();
  }

  @Override
  public void reload(String name, Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
    discovery.getDefinitions(new JsonObject().put("name", name), ar -> {
      for (ApiDefinition apiDefinition : ar.result()) {
        definitions.removeIf(d -> d.name().equalsIgnoreCase(apiDefinition.name()));
        definitions.add(apiDefinition);
      }
      resultHandler.handle(ar);
    });
  }

}
