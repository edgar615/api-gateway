package com.edgar.direwolves.verticle;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.edgar.direwolves.core.cmd.CmdRegister;
import com.edgar.direwolves.core.utils.LoggerUtils;
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
    LoggerUtils.info(LOGGER, "config.read", "read definition config",
                     Lists.newArrayList("config"),
                     Lists.newArrayList(config().encode()));
    initialize(startFuture);
  }

  public void initialize(Future<Void> startFuture) {
    //读取路由
    Future<Void> importApiFuture = Future.future();
    new ImportApi().initialize(vertx, config(), importApiFuture);
    //读取命令
    Future<Void> importCmdFuture = Future.future();
    new CmdRegister().initialize(vertx, config(), importCmdFuture);

    CompositeFuture.all(importApiFuture, importCmdFuture)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                LoggerUtils.info(LOGGER, "verticle.deployed", "OK",
                                 Lists.newArrayList("verticle"),
                                 Lists.newArrayList("ApiDefinitionVerticle"));
                startFuture.complete();
              } else {
                LoggerUtils.error(LOGGER, "verticle.deployed", "FAILED",
                                 Lists.newArrayList("verticle"),
                                 Lists.newArrayList("ApiDefinitionVerticle"),
                                  ar.cause());
                startFuture.fail(ar.cause());
              }
            });
  }


}
