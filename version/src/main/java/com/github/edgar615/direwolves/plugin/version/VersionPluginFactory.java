package com.github.edgar615.direwolves.plugin.version;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/11/6.
 *
 * @author Edgar  Date 2017/11/6
 */
public class VersionPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return VersionPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new VersionPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.containsKey("version")) {
      String version = jsonObject.getString("version");
      VersionPlugin plugin = new VersionPlugin();
      plugin.setVersion(version);
      return plugin;
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    VersionPlugin versionPlugin = (VersionPlugin) plugin;
    if (versionPlugin.version() != null) {
      return new JsonObject().put("version", versionPlugin.version());
    }
    return new JsonObject();
  }
}
