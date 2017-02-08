package com.edgar.direwolves.standalone;

import com.edgar.direwolves.dispatch.verticle.ApiDispatchVerticle;
import com.edgar.direwolves.verticle.ApiDefinitionVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

/**
 * Created by Edgar on 2017/2/8.
 *
 * @author Edgar  Date 2017/2/8
 */
public class StartupVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    vertx.deployVerticle(ApiDispatchVerticle.class.getName(),
                         new DeploymentOptions().setConfig(config()));
    vertx.deployVerticle(ApiDefinitionVerticle.class.getName(),
                         new DeploymentOptions().setConfig(config()));
  }
}
