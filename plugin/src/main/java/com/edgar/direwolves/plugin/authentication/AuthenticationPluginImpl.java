package com.edgar.direwolves.plugin.authentication;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
class AuthenticationPluginImpl implements AuthenticationPlugin {

  private final Set<String> authentications = new HashSet<>();

  AuthenticationPluginImpl() {
  }

  @Override
  public AuthenticationPlugin add(String authentication) {
    Preconditions.checkNotNull(authentication, "authentication cannot be null");
    this.authentications.add(authentication);
    return this;
  }

  @Override
  public AuthenticationPlugin remove(String authentication) {
    Preconditions.checkNotNull(authentication, "authentication cannot be null");
    this.authentications.remove(authentication);
    return this;
  }

  @Override
  public AuthenticationPlugin clear() {
    this.authentications.clear();
    return this;
  }

  @Override
  public List<String> authentications() {
    return ImmutableList.copyOf(authentications);
  }
}
