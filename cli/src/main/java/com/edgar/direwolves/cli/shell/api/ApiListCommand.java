package com.edgar.direwolves.cli.shell.api;

import io.vertx.core.cli.annotations.Argument;
import io.vertx.core.cli.annotations.Description;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Option;
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
@Name("api-ls")
@Summary("query api")
public class ApiListCommand extends AnnotatedCommand {
  private static final String ADDRESS = "direwolves.eb.api.list";

  private String namespace;

  private String name;

  private int start = 0;

  private int limit = 5;

  @Argument(index = 0, argName = "namespace")
  @Description("the namespace of api")
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @Argument(index = 1, argName = "name", required = false)
  @Description("the name of api")
  public void setName(String name) {
    this.name = name;
  }

  @Option(shortName = "s", longName = "start", required = false)
  public void setStart(int start) {
    if (start < 0) {
      start = 0;
    }
    this.start = start;
  }

  @Option(shortName = "l", longName = "limit", required = false)
  public void setLimit(int limit) {
    if (limit < 1) {
      limit = 5;
    }
    this.limit = limit;
  }

  @Override
  public void process(CommandProcess process) {
    VertxInternal vertx = (VertxInternal) process.vertx();
    vertx.eventBus().<JsonObject>send(ADDRESS,
                                      new JsonObject().put("namespace", namespace)
                                              .put("name", name)
                                              .put("start", start)
                                              .put("limit", limit),
                                      ar -> {
                                        if (ar.failed()) {
                                          StringWriter buffer = new StringWriter();
                                          PrintWriter writer = new PrintWriter(buffer);
                                          ar.cause().printStackTrace(writer);
                                          process.write(buffer.toString()).end();
                                          return;
                                        }
                                        JsonArray result = ar.result().body().getJsonArray
                                                ("result", new JsonArray());
                                        process.write(
                                                String.format("%-40s%-8s%-30s\n", "name", "method",
                                                              "path"));
                                        for (int i = 0; i < result.size(); i++) {
                                          JsonObject jsonObject = result.getJsonObject(i);
                                          process.write(String.format("%-40s%-8s%-30s\n",
                                                                      jsonObject.getString("name"),
                                                                      jsonObject
                                                                              .getString("method"),
                                                                      jsonObject
                                                                              .getString("path")));
                                        }
                                        process.end();
                                      });
  }
}
