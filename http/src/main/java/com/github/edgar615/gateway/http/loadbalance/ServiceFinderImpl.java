package com.github.edgar615.gateway.http.loadbalance;

import com.github.edgar615.util.log.Log;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/7/31.
 * <b>注意</b>
 * 由于backend依赖于下游资源（DB、SyncMap），ServiceDiscovery有些时候的性能可能不够理想，所以最后在ServiceDiscovery的上层再包装一个本地缓存.
 *
 * @author Edgar  Date 2017/7/31
 */
class ServiceFinderImpl implements ServiceFinder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFinder.class);

  /**
   * 本地缓存
   */
  private final List<Record> records = new CopyOnWriteArrayList<>();

  private final ServiceDiscovery discovery;

  ServiceFinderImpl(Vertx vertx, ServiceDiscovery discovery) {
    this.discovery = discovery;
    //启动时加载所有服务
    reload(ar -> {
      if (ar.succeeded()) {
        Log.create(LOGGER)
                .setEvent("service.load.succeed");
      } else {
        Log.create(LOGGER)
                .setEvent("service.load.failure")
                .setThrowable(ar.cause());
      }
    });
    //每次收到某个服务的变动，就根据服务名重新加载所有服务
    String announce = discovery.options().getAnnounceAddress();
    vertx.eventBus().<JsonObject>consumer(announce, msg -> {
      Record record = new Record(msg.body());
      String name = record.getName();
      discovery.getRecords(new JsonObject().put("name", name), ar -> {
        records.removeIf(r -> r.getName().endsWith(name));
        records.addAll(ar.result());
      });
    });
  }

  @Override
  public void getRecord(Function<Record, Boolean> filter,
                        Handler<AsyncResult<Record>> resultHandler) {
    Optional<Record> any = records.stream()
            .filter(filter::apply)
            .findAny();
    if (any.isPresent()) {
      resultHandler.handle(Future.succeededFuture(any.get()));
    } else {
      resultHandler.handle(Future.succeededFuture(null));
    }
  }

  @Override
  public void getRecords(Function<Record, Boolean> filter,
                         Handler<AsyncResult<List<Record>>> resultHandler) {
    List<Record> result = records.stream()
            .filter(filter::apply)
            .collect(Collectors.toList());
    resultHandler.handle(Future.succeededFuture(result));
  }

  @Override
  public void reload(Handler<AsyncResult<List<Record>>> resultHandler) {
    discovery.getRecords(new JsonObject(), ar -> {
      records.clear();
      records.addAll(ar.result());
      resultHandler.handle(ar);
    });
  }
}
