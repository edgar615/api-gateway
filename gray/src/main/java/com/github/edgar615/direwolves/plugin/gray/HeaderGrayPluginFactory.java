package com.github.edgar615.direwolves.plugin.gray;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/11/7.
 *
 * @author Edgar  Date 2017/11/7
 */
public class HeaderGrayPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return HeaderGrayPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new HeaderGrayPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getValue("gray.header") instanceof String) {
      String type = jsonObject.getString("gray.header", "floor");
      if ("floor".equalsIgnoreCase(type)) {
        HeaderGrayPlugin plugin = new HeaderGrayPlugin();
        plugin.floor();
        return plugin;
      }
      if ("ceil".equalsIgnoreCase(type)) {
        HeaderGrayPlugin plugin = new HeaderGrayPlugin();
        plugin.ceil();
        return plugin;
      }
    }

    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    HeaderGrayPlugin headerGrayPlugin = (HeaderGrayPlugin) plugin;
    if (headerGrayPlugin != null) {
      return new JsonObject().put("gray.header", headerGrayPlugin.type());
    }
    return new JsonObject();
  }
}
