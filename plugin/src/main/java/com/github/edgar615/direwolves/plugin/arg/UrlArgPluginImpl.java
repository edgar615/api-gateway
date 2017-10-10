package com.github.edgar615.direwolves.plugin.arg;

import com.google.common.base.MoreObjects;

/**
 * Created by edgar on 16-10-22.
 */
class UrlArgPluginImpl extends ArgPluginImpl implements UrlArgPlugin {

  UrlArgPluginImpl() {
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("BodyArgPlugin")
            .add("parameters", parameters())
            .toString();
  }
}
