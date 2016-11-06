package com.edgar.direwolves.plugin.response;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-11-5.
 */
public class ExtractValueFactory implements ApiPluginFactory<ExtractValuePlugin> {
  @Override
  public String name() {
    return ExtractValuePlugin.NAME;
  }

  @Override
  public ApiPlugin create() {
    return new ExtractValuePluginImpl();
  }

  @Override
  public ExtractValuePlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("extractvalue", false)) {
      return new ExtractValuePluginImpl();
    }
    return null;
  }

  @Override
  public JsonObject encode(ExtractValuePlugin obj) {
    return new JsonObject().put("extractvalue", true);
  }
}
