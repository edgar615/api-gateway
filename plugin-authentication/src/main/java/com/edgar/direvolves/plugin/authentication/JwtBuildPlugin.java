package com.edgar.direvolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * Created by edgar on 16-11-26.
 */
public interface JwtBuildPlugin extends ApiPlugin {

  @Override
  default String name() {
    return JwtBuildPlugin.class.getSimpleName();
  }
}
