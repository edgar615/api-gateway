package com.edgar.direwolves.cli.shell.cluster;

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
import java.util.List;

/**
 * Created by Edgar on 2017/6/19.
 *
 * @author Edgar  Date 2017/6/19
 */
@Name("node-ls")
@Summary("List all node")
public class NodeLsCommand extends AnnotatedCommand {

  @Override
  public void process(CommandProcess process) {
    if (!process.vertx().isClustered()) {
      process.write("localhost\n").end();
      process.end();
    }
    VertxInternal vertx = (VertxInternal) process.vertx();
    List<String> nodes = vertx.getClusterManager().getNodes();
    nodes.forEach(n -> process.write(n).write("\n"));
    process.end();
  }
}
