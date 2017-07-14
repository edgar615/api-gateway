package com.edgar.direwolves.core.apidiscovery;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.utils.Log;
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
class ApiLocalCacheImpl implements ApiLocalCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiLocalCacheImpl.class);

  private final List<ApiDefinition> definitions = new CopyOnWriteArrayList<>();

  private ApiDiscovery discovery;

  ApiLocalCacheImpl(Vertx vertx, ApiDiscoveryOptions options) {
    this.discovery = ApiDiscovery.create(vertx, options);
    //todo 目前数据较少，直接加载全部数据，后面如果数据较多，可能需要考虑分块加载
    discovery.getDefinitions(r -> true, ar -> {
      if (ar.failed()) {
        Log.create(LOGGER)
                .setEvent("cache.load")
                .setThrowable(ar.cause());
        return;
      }
      definitions.addAll(ar.result());
    });

    String publishedAddress = options.getName() + "." + options.getPublishedAddress();
    String unpublishedAddress = options.getName() + "." + options.getUnpublishedAddress();
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

}
