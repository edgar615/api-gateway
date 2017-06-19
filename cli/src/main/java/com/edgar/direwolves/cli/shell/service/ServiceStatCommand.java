package com.edgar.direwolves.cli.shell.service;

import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.command.AnnotatedCommand;
import io.vertx.ext.shell.command.CommandProcess;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

/**
 * Created by Edgar on 2017/6/19.
 *
 * @author Edgar  Date 2017/6/19
 */
@Name("service-stat")
@Summary("stat all service")
public class ServiceStatCommand extends AnnotatedCommand {
  private static final String ADDRESS = "service.discovery.queryForNames";

  @Override
  public void process(CommandProcess process) {
    VertxInternal vertx = (VertxInternal) process.vertx();
    //由于是异步，该命令有时候会在控制台显示service-ls，尚未检查内部原因
    vertx.eventBus().<JsonObject>send(ADDRESS, new JsonObject(), ar -> {
      process.write(String.format("%-20s%-3s\n", "name", "instances"));
      if (ar.failed()) {
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        ar.cause().printStackTrace(writer);
        process.write(buffer.toString()).end();
        return;
      }
      JsonObject result = ar.result().body();
      Set<String> fields = result.fieldNames();
      for (String field : fields) {
        JsonObject service = result.getJsonObject(field, new JsonObject());
        process.write(String.format("%-20s%-3d\n", field, service.getInteger("instances", 0)));
      }
      process.end();
    });
  }
}
