package com.github.edgar615.direwolves.plugin.scope;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;

/**
 * 权限校验策略.
 * 该插件对应的JSON配置的key为<b>scope</b>
 * <pre>
 *   scope 接口的权限值 默认值 default
 * </pre>
 * <p>
 * Created by edgar on 16-12-25.
 */
public interface ScopePlugin extends ApiPlugin {

  static ScopePlugin create(String scope) {
    return new ScopePluginImpl(scope);
  }

  String scope();

  @Override
  default String name() {
    return ScopePlugin.class.getSimpleName();
  }
}
