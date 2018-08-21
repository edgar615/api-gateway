package com.github.edgar615.gateway.core.apidiscovery;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Api导入接口.
 *
 * @author Edgar  Date 2017/7/14
 */
public interface ApiImporter {

    /**
     * Starts the importer.
     *
     * @param vertx     the vertx instance
     * @param publisher the api discovery instance
     * @param config    the bridge configuration if any
     * @param future    a future on which the bridge must report the completion of the starting
     */
    void start(Vertx vertx, ApiPublisher publisher, JsonObject config,
               Future<Void> future);

    void restart(Future<Void> complete);

    /**
     * Closes the importer
     *
     * @param closeHandler the handle to be notified when importer is closed, may be {@code null}
     */
    default void close(Handler<Void> closeHandler) {
        closeHandler.handle(null);
    }

}
