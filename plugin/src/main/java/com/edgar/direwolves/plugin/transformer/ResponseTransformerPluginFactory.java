package com.edgar.direwolves.plugin.transformer;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

import java.util.stream.Collectors;

/**
 * Response Transformer的工厂类.
 * <p>
 * Created by edgar on 16-10-23.
 */
public class ResponseTransformerPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return ResponseTransformerPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new ResponseTransformerPluginImpl();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("response.transformer")) {
      return null;
    }
    ResponseTransformerPlugin plugin = new ResponseTransformerPluginImpl();
    JsonObject resp = jsonObject.getJsonObject("response.transformer");
    ResponseTransformerConverter.fromJson(resp, plugin);

    return plugin;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    ResponseTransformerPlugin transformerPlugin = (ResponseTransformerPlugin) plugin;
    return new JsonObject().put("response.transformer", toJson(transformerPlugin));
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


}
