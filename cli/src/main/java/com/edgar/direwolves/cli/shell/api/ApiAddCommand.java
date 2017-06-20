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
@Name("api-add")
@Summary("add an api")
public class ApiAddCommand extends AnnotatedCommand {
  private static final String ADDRESS = "direwolves.eb.api.add";

  private String namespace;

  private String data;

  @Argument(index = 0, argName = "namespace")
  @Description("the namespace of api")
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @Argument(index = 1, argName = "data")
  @Description("the json of api")
  public void setData(String data) {
    this.data = data;
  }

  @Override
  public void process(CommandProcess process) {
    VertxInternal vertx = (VertxInternal) process.vertx();
    vertx.eventBus().<JsonObject>send(ADDRESS,
                                      new JsonObject().put("namespace", namespace)
                                              .put("data", data),
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
