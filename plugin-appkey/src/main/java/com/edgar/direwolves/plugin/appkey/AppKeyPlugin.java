package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * Created by edgar on 16-10-31.
 */
public interface AppKeyPlugin extends ApiPlugin {
  @Override
  default String name() {
    return AppKeyPlugin.class.getSimpleName();
  }
}
