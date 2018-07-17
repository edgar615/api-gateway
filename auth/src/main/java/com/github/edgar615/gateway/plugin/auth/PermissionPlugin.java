package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

/**
 * 权限校验策略.
 * 该插件对应的JSON配置的key为<b>permission</b>
 * <pre>
 *   permission 接口的权限值 默认值 default
 * </pre>
 * <p>
 * Created by edgar on 16-12-25.
 */
public interface PermissionPlugin extends ApiPlugin {

  static PermissionPlugin create(String scope) {
    return new PermissionPluginImpl(scope);
  }

  String permission();

  @Override
  default String name() {
    return PermissionPlugin.class.getSimpleName();
  }
}
