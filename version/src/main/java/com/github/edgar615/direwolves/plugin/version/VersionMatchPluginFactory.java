package com.github.edgar615.direwolves.plugin.version;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/11/7.
 *
 * @author Edgar  Date 2017/11/7
 */
public class VersionMatchPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return VersionMatchPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new VersionMatchPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getValue("version.match") instanceof String) {
      String type = jsonObject.getString("version.match", "floor");
      if ("floor".equalsIgnoreCase(type)) {
        VersionMatchPlugin plugin = new VersionMatchPlugin();
        plugin.floor();
        return plugin;
      }
      if ("ceil".equalsIgnoreCase(type)) {
        VersionMatchPlugin plugin = new VersionMatchPlugin();
        plugin.ceil();
        return plugin;
      }
    }

    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    VersionMatchPlugin versionMatchPlugin = (VersionMatchPlugin) plugin;
    if (versionMatchPlugin != null) {
      return new JsonObject().put("version.match", versionMatchPlugin.type());
    }
    return new JsonObject();
  }
}
