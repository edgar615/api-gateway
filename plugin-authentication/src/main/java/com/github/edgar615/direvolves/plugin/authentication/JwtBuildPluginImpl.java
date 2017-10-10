package com.github.edgar615.direvolves.plugin.authentication;

import com.google.common.base.MoreObjects;

/**
 * Created by edgar on 16-11-26.
 */
class JwtBuildPluginImpl implements JwtBuildPlugin {

  JwtBuildPluginImpl() {
  }

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper("JwtBuildPlugin")
            .toString();
  }
}
