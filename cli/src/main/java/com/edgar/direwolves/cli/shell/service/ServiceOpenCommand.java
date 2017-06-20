package com.edgar.direwolves.cli.shell.service;

import io.vertx.core.cli.annotations.Argument;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.command.AnnotatedCommand;
import io.vertx.ext.shell.command.CommandProcess;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Edgar on 2017/6/19.
 *
 * @author Edgar  Date 2017/6/19
 */
@Name("service-open")
@Summary("open an service")
public class ServiceOpenCommand extends AbstractServiceCommand {
  private static final String ADDRESS = "service.discovery.open";

  @Override
  public String address() {
    return ADDRESS;
  }
}
