package com.github.edgar615.direvolves.plugin.auth;

import com.google.common.base.MoreObjects;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;

/**
 * 身份验证插件.
 *  * <p>
 * 该插件对应的JSON配置的key为<b>token</b>，它对应的值是一个bool值:
 * <pre>
 *   true 开启
 *   false 关闭
 * </pre>
 *
 * @author Edgar  Date 2016/10/31
 */
public class UserLoaderPlugin implements ApiPlugin {

  @Override
  public String name() {
    return UserLoaderPlugin.class.getSimpleName();
  }


  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper("UserLoaderPlugin")
            .toString();
  }
}
