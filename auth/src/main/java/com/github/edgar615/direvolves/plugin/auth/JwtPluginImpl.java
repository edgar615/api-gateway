package com.github.edgar615.direvolves.plugin.auth;

import com.google.common.base.MoreObjects;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
class JwtPluginImpl implements JwtPlugin {

  JwtPluginImpl() {
  }

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper("JwtPlugin")
            .toString();
  }
}
