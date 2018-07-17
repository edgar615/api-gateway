package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

/**
 * 创建jwt token的插件.
 * 该插件会在返回的结果里增加一个token属性.
 * * <p>
 * 该插件对应的JSON配置的key为<b>jwt.build</b>，它对应的值是一个bool值:
 * <pre>
 *   true 开启
 *   false 关闭
 * </pre>
 *
 * @author Edgar  Date 2016/10/31
 */
public interface JwtBuildPlugin extends ApiPlugin {

  @Override
  default String name() {
    return JwtBuildPlugin.class.getSimpleName();
  }
}
