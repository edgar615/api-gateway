package com.edgar.direwolves.plugin.authorization;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * Created by edgar on 16-12-25.
 */
public interface AuthorityPlugin extends ApiPlugin {

  @Override
  default String name() {
    return AuthorityPlugin.class.getSimpleName();
  }

  String scope();
}
