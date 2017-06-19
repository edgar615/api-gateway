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
import java.util.Set;

/**
 * Created by Edgar on 2017/6/19.
 *
 * @author Edgar  Date 2017/6/19
 */
@Name("service-info")
@Summary("find service by id")
public class ServiceInfoCommand extends AnnotatedCommand {
  private static final String ADDRESS = "service.discovery.queryById";

  private String id;

  @Argument(index = 0, argName = "id")
  @Description("the service id")
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void process(CommandProcess process) {
    VertxInternal vertx = (VertxInternal) process.vertx();
    vertx.eventBus().<JsonObject>send(ADDRESS, new JsonObject().put("id", id), ar -> {
      if (ar.failed()) {
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        ar.cause().printStackTrace(writer);
        process.write(buffer.toString()).end();
        return;
      }
      JsonObject result = ar.result().body();
      process.write("id:")
              .write(result.getString("registration"))
              .write("\n")
              .write("name:")
              .write(result.getString("name"))
              .write("\n")
              .write("type:")
              .write(result.getString("type"))
              .write("\n")
              .write("status:")
              .write(result.getString("status"))
              .write("\n");
      process.write("location:\n");
      JsonObject location = result.getJsonObject("location");
      for (String key : location.fieldNames()) {
        Object value = location.getValue(key);
        if (value != null) {
          process.write("        ");
          process.write(key);
          process.write(": ");
          process.write(location.getValue(key).toString());
          process.write("\n");
        }
      }
      process.write("metadata:\n");
      JsonObject metadata = result.getJsonObject("metadata");
      for (String key : metadata.fieldNames()) {
        Object value = metadata.getValue(key);
        if (value != null) {
          process.write("        ");
          process.write(key);
          process.write(": ");
          process.write(metadata.getValue(key).toString());
          process.write("\n");
        }
      }
      process.end();
    });
  }
}
