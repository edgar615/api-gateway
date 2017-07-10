package com.edgar.direwolves.cli.shell.service;

import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;

/**
 * Created by Edgar on 2017/6/19.
 *
 * @author Edgar  Date 2017/6/19
 */
@Name("service-close")
@Summary("close a service")
public class ServiceCloseCommand extends AbstractServiceCommand {
  private static final String ADDRESS = "service.discovery.close";

  @Override
  public String address() {
    return ADDRESS;
  }
}
