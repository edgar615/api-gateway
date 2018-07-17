package com.github.edgar615.gateway.verticle;

import com.google.common.base.Strings;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.util.log.Log;
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

  private static final String RELOAD_ADDR_PREFIX = "__com.github.edgar615.gateway.api.discovery.reload.";

  private static final String LOG_TYPE=FileApiDiscoveryVerticle.class.getSimpleName();

  private static final String LOG_EVENT = "deploying";

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Log.create(LOGGER)
            .setLogType(LOG_TYPE)
            .setEvent(LOG_EVENT)
            .addData("config", config().encode())
            .info();
    String path = null;
    if (config().getValue("path") instanceof String) {
      path = config().getString("path");
    }
    if (Strings.isNullOrEmpty(path)) {
      Log.create(LOGGER)
              .setLogType(LOG_TYPE)
              .setEvent(LOG_EVENT)
              .setMessage("path required")
              .error();
      startFuture.fail(LOG_EVENT);
      return;
    }
    JsonObject discoveryConfig = null;
    if (config().getValue("api.discovery") instanceof JsonObject) {
      discoveryConfig = config().getJsonObject("api.discovery");
    }
    if (discoveryConfig == null) {
      Log.create(LOGGER)
              .setLogType(LOG_TYPE)
              .setEvent(LOG_EVENT)
              .setMessage("api.discovery required")
              .error();
      startFuture.fail("api.discovery required");
      return;
    }
    ApiDiscovery discovery
            = ApiDiscovery.create(vertx,
                                  new ApiDiscoveryOptions(
                                          config().getJsonObject("api.discovery")));
    JsonObject importConfig = new JsonObject()
            .put("path", path);
    FileApiImporter importer = new FileApiImporter();
    discovery.registerImporter(importer, importConfig, ar -> {
      if (ar.succeeded()) {
        Log.create(LOGGER)
                .setLogType("ApiImporter")
                .setEvent("registered")
                .info();
        startFuture.complete();
      } else {
        Log.create(LOGGER)
                .setLogType("ApiImporter")
                .setEvent("registered.failed")
                .setThrowable(ar.cause())
                .error();
        startFuture.fail(ar.cause());
      }
    });

    String reloadAddr = RELOAD_ADDR_PREFIX + discovery.name();
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
