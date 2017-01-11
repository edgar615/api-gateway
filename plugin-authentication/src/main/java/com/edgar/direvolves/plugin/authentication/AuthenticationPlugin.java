package com.edgar.direvolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * 身份验证插件.
 *  * <p>
 * 该插件对应的JSON配置的key为<b>authentication</b>，它对应的值是一个bool值:
 * <pre>
 *   true 开启
 *   false 关闭
 * </pre>
 *
 * @author Edgar  Date 2016/10/31
 */
public interface AuthenticationPlugin extends ApiPlugin {

  @Override
  default String name() {
    return AuthenticationPlugin.class.getSimpleName();
  }

}
