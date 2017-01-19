package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * 更新APPKEY的插件.
 * 该插件会更新缓存中的appKey
 * * <p>
 * 该插件对应的JSON配置的key为<b>appkey_update</b>，它对应的值是一个bool值:
 * <pre>
 *   true 开启
 *   false 关闭
 * </pre>
 *
 * @author Edgar  Date 2016/10/31
 */
public interface AppKeyUpdatePlugin extends ApiPlugin {

  @Override
  default String name() {
    return AppKeyUpdatePlugin.class.getSimpleName();
  }
}
