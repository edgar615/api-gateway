package com.edgar.servicediscovery.verticle;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Edgar on 2017/6/9.
 *
 * @author Edgar  Date 2017/6/9
 */
abstract class AbstractInstanceConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInstanceConsumer.class);

  final ServiceDiscovery discovery;

  AbstractInstanceConsumer(ServiceDiscovery discovery) {
    this.discovery = discovery;
  }

  protected void update(Record record, Handler<AsyncResult<Void>> handler) {
    discovery.update(record, ar -> {
      if (ar.failed()) {
        LOGGER.error("[{}] [update service]", record.getRegistration(), record.toJson().encode(),
                     ar.cause());
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      LOGGER.info("[{}] [update service] [{}]", record.getRegistration(), record.toJson().encode());
      handler.handle(Future.succeededFuture());
    });
  }

  protected void changeState(String id, String state, Handler<AsyncResult<Void>> handler) {
    discovery.getRecord(r -> r.getRegistration().equalsIgnoreCase(id), ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      Record record = ar.result();
      record.getMetadata().put("state", state);
      update(record, handler);
    });
  }
}
