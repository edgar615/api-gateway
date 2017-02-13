package com.edgar.direwolves.plugin.appkey;

import com.google.common.base.MoreObjects;

/**
 * Created by edgar on 16-11-26.
 */
class AppKeyUpdatePluginImpl implements AppKeyUpdatePlugin {

  AppKeyUpdatePluginImpl() {
  }

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper("AppKeyUpdatePlugin")
            .toString();
  }
}
