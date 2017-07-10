package com.edgar.direwolves.cli.shell.service;

import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;

/**
 * Created by Edgar on 2017/6/19.
 *
 * @author Edgar  Date 2017/6/19
 */
@Name("service-open")
@Summary("open a service")
public class ServiceOpenCommand extends AbstractServiceCommand {
  private static final String ADDRESS = "service.discovery.open";

  @Override
  public String address() {
    return ADDRESS;
  }
}
