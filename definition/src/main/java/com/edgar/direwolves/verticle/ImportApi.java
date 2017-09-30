package com.edgar.direwolves.verticle;

import com.edgar.direwolves.cmd.ImportApiCmd;
import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.util.log.Log;
import com.edgar.util.vertx.spi.Initializable;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 启动时加载API.
 *
 * @author Edgar  Date 2017/3/30
 */
public class ImportApi implements Initializable {

  Logger LOGGER = LoggerFactory.getLogger(ImportApi.class);

  @Override
  public void initialize(Vertx vertx, JsonObject config, Future<Void> complete) {
    JsonObject discoveryConfig = config.getJsonObject("api.discovery", new JsonObject());
    JsonObject importer = discoveryConfig.getJsonObject("importer", new JsonObject());
    discoveryConfig.remove("importer");

    List<Future> futures = new ArrayList<>();
    for (String namespace : importer.fieldNames()) {
      JsonObject _config = importer.getJsonObject(namespace);
      String path = _config.getString("file");
      ApiCmd cmd = new ImportApiCmd(vertx, config);
      Future<JsonObject> imported = cmd.handle(new JsonObject().put("path", path)
                                                       .put("namespace", namespace));
      futures.add(imported);
      Log.create(LOGGER)
              .setEvent("api.import")
              .addData("namespace", namespace)
              .addData("path", path)
              .info();
    }
    if (futures.isEmpty()) {
      complete.complete();
      return;
    }

    CompositeFuture.all(futures)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                complete.complete();
              } else {
                complete.fail(ar.cause());
              }
              for (int i = 0; i < ar.result().size(); i++) {
                if (ar.result().succeeded(i)) {
                  Log.create(LOGGER)
                          .setEvent("api.import.succeeded")
                          .addData("result", ar.result().resultAt(i))
                          .info();
                } else {
                  Log.create(LOGGER)
                          .setEvent("api.import.failed")
                          .setThrowable(ar.cause())
                          .error();
                }
              }
            });

  }
}
