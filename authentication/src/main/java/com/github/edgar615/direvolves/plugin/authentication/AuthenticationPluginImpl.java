package com.github.edgar615.direvolves.plugin.authentication;

import com.google.common.base.MoreObjects;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
class AuthenticationPluginImpl implements AuthenticationPlugin {

  AuthenticationPluginImpl() {
  }

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper("AuthenticationPlugin")
            .toString();
  }
}
