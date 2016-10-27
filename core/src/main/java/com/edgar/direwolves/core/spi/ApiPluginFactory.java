package com.edgar.direwolves.core.spi;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public interface ApiPluginFactory<T extends ApiPlugin> extends JsonObjectCodec<T> {
  /**
   * @return 插件名称
   */
  String name();

  ApiPlugin create();
}
