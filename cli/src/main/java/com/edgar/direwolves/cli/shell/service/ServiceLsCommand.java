package com.edgar.direwolves.cli.shell.service;

import io.vertx.core.cli.annotations.Argument;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonArray;
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
@Name("service-ls")
@Summary("List all service")
public class ServiceLsCommand extends AnnotatedCommand {
  private static final String ADDRESS = "service.discovery.query";

  private String name;

  @Argument(index = 0, argName = "name", required = false)
  @Description("the service name")
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void process(CommandProcess process) {
    VertxInternal vertx = (VertxInternal) process.vertx();
    JsonObject arg = new JsonObject();
    if (name != null) {
      arg.put("name", name);
    }
    vertx.eventBus().<JsonArray>send(ADDRESS, arg, ar -> {
      process.write(String.format("%-40s%-20s%-6s\n", "id", "name", "weight"));
      if (ar.failed()) {
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        ar.cause().printStackTrace(writer);
        process.write(buffer.toString()).end();
        return;
      }
      JsonArray result = ar.result().body();
      for (int i = 0; i < result.size(); i++) {
        JsonObject service = result.getJsonObject(i);
        process.write(String.format("%-40s%-20s%-6d\n", service.getString("registration"),
                                    service.getString("name"),
                                    service.getJsonObject("metadata", new JsonObject())
                                            .getInteger("weight")));
      }
      process.end();
    });
  }
}
