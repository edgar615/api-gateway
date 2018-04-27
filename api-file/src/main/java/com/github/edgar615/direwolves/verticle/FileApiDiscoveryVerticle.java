package com.github.edgar615.direwolves.verticle;

import com.github.edgar615.direwolves.core.utils.Log;
import com.google.common.base.Strings;

import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscoveryOptions;
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

  private static final String APPLICATION = FileApiDiscoveryVerticle.class.getSimpleName();

  private static final String RELOAD_ADDR_PREFIX = "__com.github.edgar615.direwolves.api.discovery.reload.";

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Log.create(LOGGER)
            .setApplication(APPLICATION)
            .setEvent("deploying")
            .addData("config", config().encode())
            .info();
    String path = null;
    if (config().getValue("path") instanceof String) {
      path = config().getString("path");
    }
    if (Strings.isNullOrEmpty(path)) {
      Log.create(LOGGER)
              .setApplication(APPLICATION)
              .setEvent("deploy.failed")
              .setMessage("api path is null")
              .error();
      startFuture.fail("api path is null");
      return;
    }
    JsonObject discoveryConfig = null;
    if (config().getValue("api.discovery") instanceof JsonObject) {
      discoveryConfig = config().getJsonObject("api.discovery");
    }
    if (discoveryConfig == null) {
      Log.create(LOGGER)
              .setApplication(APPLICATION)
              .setEvent("deploy.failed")
              .setMessage("api.discovery is null")
              .error();
      startFuture.fail("api.discovery is null");
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
                .setApplication(APPLICATION)
                .setEvent("deploy.succeeded")
                .info();
        startFuture.complete();
      } else {
        Log.create(LOGGER)
                .setApplication(APPLICATION)
                .setEvent("deploy.failed")
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
