package com.edgar.direwolves.verticle;

import com.edgar.direwolves.cmd.ImportApiCmd;
import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.util.vertx.spi.Initializable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 启动时加载API.
 *
 * @author Edgar  Date 2017/3/30
 */
public class ImportApi implements Initializable {
  @Override
  public void initialize(Vertx vertx, JsonObject config, Future<Void> complete) {
    if (config.containsKey("api.config.dir")) {
      String configDir = config.getString("api.config.dir");
      ApiCmd cmd = new ImportApiCmd(vertx);
      Future<JsonObject> imported = cmd.handle(new JsonObject().put("path", configDir));
      imported.setHandler(ar -> {
        if (ar.succeeded()) {
//        LOGGER.info("---| [Import Api] [OK] [{}] [{}]", configDir, ar.result().encode());
          complete.complete();
        } else {
          complete.fail(ar.cause());
        }
      });
    } else {
      complete.complete();
    }

  }
}
