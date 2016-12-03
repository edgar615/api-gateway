package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * Created by edgar on 16-11-26.
 */
public interface JwtCreatePlugin extends ApiPlugin {

  @Override
  default String name() {
    return JwtCreatePlugin.class.getSimpleName();
  }
}
