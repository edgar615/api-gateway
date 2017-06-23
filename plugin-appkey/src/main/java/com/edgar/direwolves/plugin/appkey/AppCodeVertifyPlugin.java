package com.edgar.direwolves.plugin.appkey;

import com.google.common.base.MoreObjects;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * 检查用户的appCode和appkey的appCode是否一致.
 * <p>
 * 该插件对应的JSON配置的key为<b>app.code.vertify</b>，它对应的值是一个bool值:
 * <pre>
 *   true 开启校验
 *   false 关闭的校验
 * </pre>
 * </pre>
 *
 * @author Edgar  Date 2016/9/14
 */
public class AppCodeVertifyPlugin implements ApiPlugin {
  @Override
  public String name() {
    return AppCodeVertifyPlugin.class.getSimpleName();
  }

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper("AppCodeVertifyPlugin")
            .toString();
  }
}
