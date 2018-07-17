package com.github.edgar615.gateway.plugin.appkey;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

/**
 * APPKEY校验的.
 * <p>
 * 该插件对应的JSON配置的key为<b>appkey</b>，它对应的值是一个bool值:
 * <pre>
 *   true 开启appKey的校验
 *   false 关闭appKey的校验
 * </pre>
 * </pre>
 *
 * @author Edgar  Date 2016/9/14
 */
public interface AppKeyPlugin extends ApiPlugin {
  @Override
  default String name() {
    return AppKeyPlugin.class.getSimpleName();
  }
}
