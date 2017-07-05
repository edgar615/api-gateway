package com.edgar.direwolves.verticle;

import com.edgar.direwolves.core.cmd.CmdRegister;
import com.edgar.direwolves.core.utils.Log;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API定义的Verticle，从指定的路径<code>api.config.dir</code>读取API定义.
 *
 * @author Edgar  Date 2016/9/13
 */
public class ApiDefinitionVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiDefinitionVerticle.class);

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Log.create(LOGGER)
            .setModule("definition")
            .setEvent("config.read")
            .addData("config", config())
            .info();

    initialize(startFuture);
  }

  public void initialize(Future<Void> startFuture) {
    //读取路由
    Future<Void> importApiFuture = Future.future();
    new ApiImporter().initialize(vertx, config(), importApiFuture);
    //读取命令
    Future<Void> importCmdFuture = Future.future();
    new CmdRegister().initialize(vertx, config(), importCmdFuture);

    CompositeFuture.all(importApiFuture, importCmdFuture)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                Log.create(LOGGER)
                        .setModule("definition")
                        .setEvent("verticle.deployed")
                        .addData("verticle", this.getClass())
                        .info();
                startFuture.complete();
              } else {
                Log.create(LOGGER)
                        .setModule("definition")
                        .setEvent("verticle.deployed")
                        .addData("verticle", this.getClass())
                        .setThrowable(ar.cause())
                        .info();
                startFuture.fail(ar.cause());
              }
            });
  }


}
