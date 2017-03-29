package com.edgar.direwolves.verticle;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.edgar.direwolves.cmd.ImportApiCmd;
import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.cmd.ApiCmdFactory;
import com.edgar.direwolves.core.definition.ApiProvider;
import com.edgar.direwolves.definition.ApiProviderImpl;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * java -jar definition-1.0.0.jar run com.edgar.direwolves.definition.DefinitonVerticle.
 * <p>
 * java -jar definition-1.0.0.jar start com.edgar.direwolves.definition.DefinitonVerticle.
 * <p>
 * java -jar definition-1.0.0.jar list
 * <p>
 * java -jar definition-1.0.0.jar stop vertId
 *
 * @author Edgar  Date 2016/9/13
 */
public class ApiDefinitionVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiDefinitionVerticle.class);

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    LOGGER.info("---@ [Definition] [Read config] [OK] [{}]", config().encode());
    registerEventBusConsumer();

    String namespace = config().getString("project.namespace", "");
    String address = ApiProvider.class.getName();
    if (!Strings.isNullOrEmpty(namespace)) {
      address = namespace + "." + address;
    }
    ProxyHelper.registerService(ApiProvider.class, vertx, new ApiProviderImpl(), address);
    LOGGER.info("---@ [Definition] [register ApiProvider] [OK] [{}]", address);
    //读取路由
    if (config().containsKey("api.config.dir")) {
      readApi(startFuture);
    } else {
      startFuture.complete();
    }
  }

  private void readApi(Future<Void> startFuture) {
    String configDir = config().getString("api.config.dir");
    ApiCmd cmd = new ImportApiCmd(vertx);
    Future<JsonObject> imported = cmd.handle(new JsonObject().put("path", configDir));
    imported.setHandler(ar -> {
      if (ar.succeeded()) {
        LOGGER.info("---| [Import Api] [OK] [{}]", ar.result().encode());
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });
  }

  private void registerEventBusConsumer() {
    String namespace = config().getString("project.namespace", "");
    //eventbus consumer
    EventBus eb = vertx.eventBus();
    Lists.newArrayList(ServiceLoader.load(ApiCmdFactory.class))
            .stream()
            .map(f -> f.create(vertx, config()))
            .forEach(cmd -> {
              LOGGER.info("---@ [Definition] [register consumer] [OK] [{}]",
                          cmdAddress(namespace, cmd.cmd()));
              eb.<JsonObject>consumer(cmdAddress(namespace, cmd.cmd()), msg -> {
                Future<JsonObject> future = cmd.handle(msg.body());
                future.setHandler(ar -> {
                  if (ar.succeeded()) {
                    msg.reply(ar.result());
                  } else {
                    eventbusFailureHandler(msg, ar.cause());
                  }
                });
              });
            });
  }

  private String cmdAddress(String namespace, String cmd) {
    if (Strings.isNullOrEmpty(namespace)) {
      return "direwolves.eb." + cmd;
    }
    return namespace + ".direwolves.eb." + cmd;
  }

  private void eventbusFailureHandler(Message<JsonObject> msg, Throwable throwable) {
    if (throwable instanceof SystemException) {
      SystemException ex = (SystemException) throwable;
      msg.fail(ex.getErrorCode().getNumber(), ex.getMessage());
    } else if (throwable instanceof ValidationException) {
      msg.fail(DefaultErrorCode.INVALID_ARGS.getNumber(),
               DefaultErrorCode.INVALID_ARGS.getMessage());
    } else {
      msg.fail(999, throwable.getMessage());
    }
  }


}
