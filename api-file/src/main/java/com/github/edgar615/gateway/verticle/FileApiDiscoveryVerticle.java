package com.github.edgar615.gateway.verticle;

import com.google.common.base.Strings;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 从文件读取API定义.
 * 仅处理*.json类型的文件
 *
 * @author Edgar  Date 2016/9/13
 */
public class FileApiDiscoveryVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileApiDiscoveryVerticle.class);

    private static final String RELOAD_ADDR_PREFIX =
            "__com.github.edgar615.gateway.api.discovery.reload.file";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOGGER.info("[Verticle] [start] start {}",
                    FileApiDiscoveryVerticle.class.getSimpleName());
        String path = null;
        if (config().getValue("path") instanceof String) {
            path = config().getString("path");
        }
        if (Strings.isNullOrEmpty(path)) {
            LOGGER.error("[Verticle] [start] start {} failed, path required",
                         FileApiDiscoveryVerticle.class.getSimpleName());
            startFuture.fail("path required");
            return;
        }
        ApiDiscovery discovery
                = ApiDiscovery.create(vertx,
                                      new ApiDiscoveryOptions(
                                              config().getJsonObject("api.discovery",
                                                                     new JsonObject())));
        JsonObject importConfig = new JsonObject()
                .put("path", path);
        FileApiImporter importer = new FileApiImporter();
        discovery.registerImporter(importer, importConfig, ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });

        String reloadAddr = RELOAD_ADDR_PREFIX;
        vertx.eventBus().consumer(reloadAddr, msg -> {
            importer.restart(Future.future());
        });

        //开启监控
        startWatcher(path, reloadAddr, importer);
    }

    private void startWatcher(String path, String reloadAddr, FileApiImporter importer) {
        boolean watch = false;
        if (config().getValue("watch") instanceof Boolean) {
            watch = config().getBoolean("watch");
        }
        if (watch) {
            JsonObject watchConfig = new JsonObject().put("path", path)
                    .put("reload.address", reloadAddr);
            vertx.deployVerticle(WatcherVerticle.class.getName(), new DeploymentOptions()
                    .setWorker(true)
                    .setConfig(watchConfig));
            vertx.eventBus().consumer(reloadAddr, msg -> {
                importer.restart(Future.future());
            });
        }
    }

}
