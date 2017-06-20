package com.edgar.direwolves.cli.shell.api;

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
@Name("api-get")
@Summary("find an api")
public class ApiGetCommand extends AnnotatedCommand {
  private static final String ADDRESS = "direwolves.eb.api.get";

  private String namespace;

  private String name;

  @Argument(index = 0, argName = "namespace")
  @Description("the namespace of api")
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @Argument(index = 1, argName = "name")
  @Description("the name of api")
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void process(CommandProcess process) {
    VertxInternal vertx = (VertxInternal) process.vertx();
    vertx.eventBus().<JsonObject>send(ADDRESS,
                                      new JsonObject().put("namespace", namespace)
                                              .put("name", name),
                                      ar -> {
                                        if (ar.failed()) {
                                          StringWriter buffer = new StringWriter();
                                          PrintWriter writer = new PrintWriter(buffer);
                                          ar.cause().printStackTrace(writer);
                                          process.write(buffer.toString()).end();
                                          return;
                                        }
                                        JsonObject result = ar.result().body();
                                        process.write(result.encode())
                                                .write("\n");
                                        process.end();
                                      });
  }
}
