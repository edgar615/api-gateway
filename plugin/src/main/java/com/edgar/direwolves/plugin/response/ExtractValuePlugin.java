package com.edgar.direwolves.plugin.response;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * Created by edgar on 16-11-5.
 */
public interface ExtractValuePlugin extends ApiPlugin {
  String NAME = "ExtractValueFromSingleKey";

  default String name() {
    return NAME;
  }


}
