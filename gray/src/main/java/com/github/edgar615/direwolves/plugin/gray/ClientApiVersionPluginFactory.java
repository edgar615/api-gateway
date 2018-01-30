package com.github.edgar615.direwolves.plugin.gray;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/11/7.
 *
 * @author Edgar  Date 2017/11/7
 */
public class ClientApiVersionPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return ClientApiVersionPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new ClientApiVersionPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getValue("ca.version") instanceof String) {
      String type = jsonObject.getString("ca.version", "floor");
      if ("floor".equalsIgnoreCase(type)) {
        ClientApiVersionPlugin plugin = new ClientApiVersionPlugin();
        plugin.floor();
        return plugin;
      }
      if ("ceil".equalsIgnoreCase(type)) {
        ClientApiVersionPlugin plugin = new ClientApiVersionPlugin();
        plugin.ceil();
        return plugin;
      }
    }

    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    ClientApiVersionPlugin clientApiVersionPlugin = (ClientApiVersionPlugin) plugin;
    if (clientApiVersionPlugin != null) {
      return new JsonObject().put("ca.version", clientApiVersionPlugin.type());
    }
    return new JsonObject();
  }
}
