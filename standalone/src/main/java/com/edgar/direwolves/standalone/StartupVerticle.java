package com.edgar.direwolves.standalone;

import com.edgar.direwolves.dispatch.verticle.ApiDispatchVerticle;
import com.edgar.direwolves.verticle.ApiDefinitionVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Edgar on 2017/2/8.
 *
 * @author Edgar  Date 2017/2/8
 */
public class StartupVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(StartupVerticle.class);
  @Override
  public void start() throws Exception {
    LOGGER.info("config->{}", config().encodePrettily());
    vertx.deployVerticle(ApiDispatchVerticle.class.getName(),
                         new DeploymentOptions().setConfig(config()), ar -> {
              if (ar.succeeded()) {
                LOGGER.info("Deploy ApiDispatchVerticle succeed");
              } else {
                LOGGER.info("Deploy ApiDispatchVerticle failed", ar.cause());
              }
            });
    vertx.deployVerticle(ApiDefinitionVerticle.class.getName(),
                         new DeploymentOptions().setConfig(config()), ar -> {
              if (ar.succeeded()) {
                LOGGER.info("Deploy ApiDefinitionVerticle succeed");
              } else {
                LOGGER.info("Deploy ApiDefinitionVerticle failed", ar.cause());
              }
            });
  }
}
