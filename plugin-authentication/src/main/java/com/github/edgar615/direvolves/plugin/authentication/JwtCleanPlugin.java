package com.github.edgar615.direvolves.plugin.authentication;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;

/**
 * 清除jwt token的插件.
 * 该插件会将缓存中的token清除
 *
 * @author Edgar  Date 2016/10/31
 */
public class JwtCleanPlugin implements ApiPlugin {

  JwtCleanPlugin() {
  }

  @Override
  public String name() {
    return JwtCleanPlugin.class.getSimpleName();
  }
}
