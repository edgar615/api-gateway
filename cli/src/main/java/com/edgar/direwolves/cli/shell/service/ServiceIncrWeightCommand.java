package com.edgar.direwolves.cli.shell.service;

import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;

/**
 * Created by Edgar on 2017/6/19.
 *
 * @author Edgar  Date 2017/6/19
 */
@Name("service-weight-incr")
@Summary("increase the weight of service")
public class ServiceIncrWeightCommand extends AbstractServiceCommand {
  private static final String ADDRESS = "service.discovery.weight.increase";

  @Override
  public String address() {
    return ADDRESS;
  }
}
