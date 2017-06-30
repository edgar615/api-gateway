package com.edgar.direwolves.verticle;

import com.google.common.base.Strings;

import com.edgar.direwolves.core.cmd.CmdRegister;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.serviceproxy.ProxyHelper;
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
    LOGGER.info("[definition.start]");
    LOGGER.info("---| [config.readed] [{}]", config().encodePrettily());
    initialize(startFuture);
  }

  public void initialize(Future<Void> startFuture) {
    //读取路由
    Future<Void> importApiFuture = Future.future();
    new ImportApi().initialize(vertx, config(), importApiFuture);
    //读取命令
    Future<Void> importCmdFuture = Future.future();
    new CmdRegister().initialize(vertx, config(), importCmdFuture);

    //注册API
//    Future<Void> backendApiFuture = Future.future();
//    new RegisterBackendApi().initialize(vertx, config(), backendApiFuture);

    CompositeFuture.all(importApiFuture, importCmdFuture)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                LOGGER.info("[definition.started]");
                startFuture.complete();
              } else {
                LOGGER.error("[definition.started]", ar.cause());
                startFuture.fail(ar.cause());
              }
            });
  }


}
