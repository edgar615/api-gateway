package com.edgar.direwolves.plugin.client;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * Created by edgar on 16-10-31.
 */
public interface AppKeyCheckerPlugin extends ApiPlugin {

  String NAME = "APP_KEY_CHECKER";

  @Override
  default String name() {
    return NAME;
  }
}
