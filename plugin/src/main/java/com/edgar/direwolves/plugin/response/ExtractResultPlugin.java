package com.edgar.direwolves.plugin.response;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * Created by edgar on 16-11-5.
 */
public interface ExtractResultPlugin extends ApiPlugin {
  @Override
  default String name() {
    return ExtractResultPlugin.class.getSimpleName();
  }
}
