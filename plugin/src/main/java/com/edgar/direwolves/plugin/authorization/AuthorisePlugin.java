package com.edgar.direwolves.plugin.authorization;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * Created by edgar on 16-12-25.
 */
public interface AuthorisePlugin extends ApiPlugin {

  String scope();

  @Override
  default String name() {
    return AuthorisePlugin.class.getSimpleName();
  }
}
