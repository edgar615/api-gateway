package com.edgar.direwolves.plugin.response;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-11-5.
 */
public class ExtractResultFactory implements ApiPluginFactory<ExtractResultPlugin> {
  @Override
  public String name() {
    return ExtractResultPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new ExtractResultPluginImpl();
  }

  @Override
  public ExtractResultPlugin decode(JsonObject jsonObject) {
//    if (jsonObject.getBoolean("extractvalue", false)) {
//      return new ExtractResultPluginImpl();
//    }
    return new ExtractResultPluginImpl();
  }

  @Override
  public JsonObject encode(ExtractResultPlugin obj) {
    return new JsonObject();
  }
}
