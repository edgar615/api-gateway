package com.edgar.direwolves.discovery;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 17-5-10.
 */
public interface ServiceImporter {

  /**
   * Starts the importer.
   *
   * @param vertx            the vertx instance
   * @param serviceDiscovery the service discovery instance
   * @param configuration    the bridge configuration if any
   * @param future           a future on which the bridge must report the completion of the starting
   */
  void start(Vertx vertx, ServiceDiscoveryInternal serviceDiscovery, JsonObject configuration,
             Future<Void> future);

  /**
   * Closes the importer
   *
   * @param closeHandler the handle to be notified when importer is closed, may be {@code null}
   */
  default void close(Handler<Void> closeHandler) {
    closeHandler.handle(null);
  }

}
