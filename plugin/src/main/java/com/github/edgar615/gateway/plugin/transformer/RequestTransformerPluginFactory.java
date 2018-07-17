package com.github.edgar615.gateway.plugin.transformer;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.stream.Collectors;

/**
 * Request Transformer的工厂类.
 * <p>
 * Created by edgar on 16-10-23.
 */
public class RequestTransformerPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return RequestTransformerPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new RequestTransformerPluginImpl();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("request.transformer")) {
      return null;
    }
    RequestTransformerPlugin plugin = new RequestTransformerPluginImpl();
    JsonArray jsonArray = jsonObject.getJsonArray("request.transformer", new JsonArray());
    for (int i = 0; i < jsonArray.size(); i++) {
      JsonObject request = jsonArray.getJsonObject(i);
      String name = request.getString("name");
      RequestTransformer transformer = RequestTransformer.create(name);
      RequestTransfomerConverter.fromJson(request, transformer);

      plugin.addTransformer(transformer);
    }

    return plugin;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    RequestTransformerPlugin transformerPlugin = (RequestTransformerPlugin) plugin;
    JsonArray jsonArray = new JsonArray();
    transformerPlugin.transformers().stream()
            .map(t -> toJson(t)
            ).forEach(j -> jsonArray.add(j));
    return new JsonObject().put("request.transformer", jsonArray);
  }

  private JsonObject toJson(RequestTransformer transformer) {
    return new JsonObject()
            .put("name", transformer.name())
            .put("header.remove", transformer.headerRemoved())
            .put("query.remove", transformer.paramRemoved())
            .put("body.remove", transformer.bodyRemoved())
            .put("header.replace", transformer.headerReplaced()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()))
            .put("query.replace", transformer.paramReplaced()
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
            .put("query.add", transformer.paramAdded()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()))
            .put("body.add", transformer.bodyAdded()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()));
  }


}
