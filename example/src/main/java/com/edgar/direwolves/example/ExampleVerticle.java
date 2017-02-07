package com.edgar.direwolves.example;

import com.edgar.direwolves.dispatch.verticle.ApiDispatchVerticle;
import com.edgar.direwolves.verticle.ApiDefinitionVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
public class ExampleVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    vertx.deployVerticle(ApiDispatchVerticle.class.getName(),
                         new DeploymentOptions().setConfig(config()));
    vertx.deployVerticle(ApiDefinitionVerticle.class.getName(),
                         new DeploymentOptions().setConfig(config()));
//    vertx.deployVerticle(ApiImporterVerticle.class.getName(),
//                         new DeploymentOptions().setConfig(config()));
  }
}
