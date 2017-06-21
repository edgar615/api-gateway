package com.edgar.direwolves.cli.shell.api;

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
class ApiUtils  {

  static void writeApi(CommandProcess process, JsonObject jsonObject) {
    process.write(String.format("%s\n", "api").replace(" ", "-"));
    process.write(String.format("   %-40s%-8s%-30s\n", "name", "method", "path"));
    process.write(String.format("   %-40s%-8s%-30s\n", jsonObject.getString("name"),
                                jsonObject.getString("method"),jsonObject.getString("path")));
    process.write(String.format("%s\n", "endpoints").replace(" ", "-"));
    JsonArray endpoints = jsonObject.getJsonArray("endpoints", new JsonArray());
    for (int i= 0; i < endpoints.size(); i ++) {
      process.write("   ");
      process.write(endpoints.getJsonObject(i).encode());
      process.write("\n");
    }
    for (String key : jsonObject.fieldNames()) {
      if (!"name".equals(key)
          && !"path".equals(key)
          && !"method".equals(key)
          && !"endpoints".equals(key)) {
        process.write(String.format("%s\n", key));
        process.write("   ");
        process.write(jsonObject.getValue(key).toString());
        process.write("\n");
      }
    }
    process.write("\n");
  }
}
