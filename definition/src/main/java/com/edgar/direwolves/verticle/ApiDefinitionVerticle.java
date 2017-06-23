package com.edgar.direwolves.verticle;

import com.google.common.base.Strings;

import com.edgar.direwolves.core.definition.ApiProvider;
import com.edgar.direwolves.definition.ApiProviderImpl;
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
    LOGGER.info("---| [Read Definition Config] [{}]", config().encodePrettily());
    String namespace = config().getString("namespace", "");
    String address = ApiProvider.class.getName();
    if (!Strings.isNullOrEmpty(namespace)) {
      address = namespace + "." + address;
    }
    ProxyHelper.registerService(ApiProvider.class, vertx, new ApiProviderImpl(), address);
    LOGGER.info("---| [Register ApiProvider] [{}]", address);
    initialize(startFuture);
  }

  public void initialize(Future<Void> startFuture) {
    //读取路由
    Future<Void> importApiFuture = Future.future();
    new ImportApi().initialize(vertx, config(), importApiFuture);
    //读取命令
    Future<Void> importCmdFuture = Future.future();
    new RegisterApiCmd().initialize(vertx, config(), importCmdFuture);

    //注册API
    Future<Void> backendApiFuture = Future.future();
    new RegisterBackendApi().initialize(vertx, config(), backendApiFuture);

    CompositeFuture.all(importApiFuture, importCmdFuture, backendApiFuture)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                LOGGER.info("---| [Definition Start] [OK]");
                startFuture.complete();
              } else {
                LOGGER.error("---| [Definition Start] [FAILED]");
                startFuture.fail(ar.cause());
              }
            });
  }


}
