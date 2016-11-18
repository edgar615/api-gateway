package com.edgar.direwolves.plugin.client;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * Created by edgar on 16-10-31.
 */
public interface AppKeyCheckerPlugin extends ApiPlugin {
  @Override
  default String name() {
    return AppKeyCheckerPlugin.class.getSimpleName();
  }
}
