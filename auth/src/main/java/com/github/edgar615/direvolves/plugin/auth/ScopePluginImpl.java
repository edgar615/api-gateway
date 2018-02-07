package com.github.edgar615.direvolves.plugin.auth;

import com.google.common.base.MoreObjects;

/**
 * Created by edgar on 16-12-25.
 */
public class ScopePluginImpl implements ScopePlugin {

  private String scope = "default";

  ScopePluginImpl() {
  }

  ScopePluginImpl(String scope) {
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
            .toStringHelper("ScopePlugin")
            .add("scope", scope)
            .toString();
  }
}
