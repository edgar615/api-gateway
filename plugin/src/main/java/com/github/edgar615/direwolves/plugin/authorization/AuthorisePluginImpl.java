package com.github.edgar615.direwolves.plugin.authorization;

import com.google.common.base.MoreObjects;

/**
 * Created by edgar on 16-12-25.
 */
public class AuthorisePluginImpl implements AuthorisePlugin {

  private String scope = "default";

  AuthorisePluginImpl() {
  }

  AuthorisePluginImpl(String scope) {
    this.scope = scope;
  }

  @Override
  public String scope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper("AuthorisePlugin")
            .add("scope", scope)
            .toString();
  }
}
