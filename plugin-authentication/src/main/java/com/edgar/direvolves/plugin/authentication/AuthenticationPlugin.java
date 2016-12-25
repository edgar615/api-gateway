package com.edgar.direvolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;

import java.util.List;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public interface AuthenticationPlugin extends ApiPlugin {

  @Override
  default String name() {
    return AuthenticationPlugin.class.getSimpleName();
  }

}
