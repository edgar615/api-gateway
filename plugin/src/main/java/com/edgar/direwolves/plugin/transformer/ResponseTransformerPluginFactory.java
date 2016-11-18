package com.edgar.direwolves.plugin.transformer;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.stream.Collectors;

/**
 * Response Transformer的工厂类.
 * json配置
 * <pre>
 *     "response_transformer" : [
 * {
 * "name" : "add_device",
 * "service": "device",
 * "method": "POST",
 * "path": "/devices",
 * "header.remove" : ["h3", "h4"],
 * "body.remove" : ["p3", "p4"],
 * "header.replace" : ["h5:v2", "h6:v1"],
 * "body.replace" : ["p5:v2", "p6:v1"],
 * "header.add" : ["h1:v2", "h2:v1"],
 * "body.add" : ["p1:v2", "p2:v1"]
 * }
 * ]
 * </pre>
 * Created by edgar on 16-10-23.
 */
public class ResponseTransformerPluginFactory implements
        ApiPluginFactory<ResponseTransformerPlugin> {
  @Override
  public String name() {
    return ResponseTransformerPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new ResponseTransformerPluginImpl();
  }

  @Override
  public ResponseTransformerPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("response_transformer")) {
      return null;
    }
    ResponseTransformerPlugin plugin = new ResponseTransformerPluginImpl();
    JsonObject request = jsonObject.getJsonObject("response_transformer");
    removeBody(request, plugin);
    removeHeader(request, plugin);
    replaceBody(request, plugin);
    replaceHeader(request, plugin);
    addBody(request, plugin);
    addHeader(request, plugin);

    return plugin;
  }

  @Override
  public JsonObject encode(ResponseTransformerPlugin plugin) {
    return new JsonObject().put("response_transformer", toJson(plugin));
  }

  private JsonObject toJson(ResponseTransformerPlugin transformer) {
    return new JsonObject()
            .put("header.remove", transformer.headerRemoved())
            .put("body.remove", transformer.bodyRemoved())
            .put("header.replace", transformer.headerReplaced()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()))
            .put("body.replace", transformer.bodyReplaced()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()))
            .put("header.add", transformer.headerAdded()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()))
            .put("body.add", transformer.bodyAdded()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()));
  }

  private void removeHeader(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray removes = endpoint.getJsonArray("header.remove", new JsonArray());
    for (int j = 0; j < removes.size(); j++) {
      transformer.removeHeader(removes.getString(j));
    }
  }

  private void removeBody(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray removes = endpoint.getJsonArray("body.remove", new JsonArray());
    for (int j = 0; j < removes.size(); j++) {
      transformer.removeBody(removes.getString(j));
    }
  }

  private void replaceHeader(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray replaces = endpoint.getJsonArray("header.replace", new JsonArray());
    for (int j = 0; j < replaces.size(); j++) {
      String value = replaces.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      transformer
              .replaceHeader(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

  private void replaceBody(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray replaces = endpoint.getJsonArray("body.replace", new JsonArray());
    for (int j = 0; j < replaces.size(); j++) {
      String value = replaces.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      transformer
              .replaceBody(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

  private void addHeader(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray adds = endpoint.getJsonArray("header.add", new JsonArray());
    for (int j = 0; j < adds.size(); j++) {
      String value = adds.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      transformer.addHeader(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

  private void addBody(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray adds = endpoint.getJsonArray("body.add", new JsonArray());
    for (int j = 0; j < adds.size(); j++) {
      String value = adds.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      transformer.addBody(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }
}
