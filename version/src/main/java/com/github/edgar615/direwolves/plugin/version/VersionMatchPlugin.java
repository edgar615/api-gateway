package com.github.edgar615.direwolves.plugin.version;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;

/**
 * 基于请求头的灰度发布插件.
 * 由于最初的架构并未考虑到灰度方案，为了将代码的改动降低到最低，通过一个灰度发布的适配类类处理灰度发布。
 * 如果某个API需要使用灰度发布，那么API的定义处理新旧两个版本外，还需要额外定义一个包含了灰度方案的Api.
 * <p>
 * 在根据请求搜索到多个API后，首先判断有没有灰度方案，如果没有对应的灰度方案，就抛出异常，
 * 如果找到了灰度方案，就按照灰度方案匹配到一个具体的API
 *
 * @author Edgar  Date 2017/11/6
 */
public class VersionMatchPlugin implements ApiPlugin {

  /**
   * floor默认取最低版本，ceil默认取最高的版本
   */
  private String type;

  public String type() {
    return type;
  }

  public VersionMatchPlugin() {
    this.type = "floor";
  }

  public VersionMatchPlugin floor() {
    this.type = "floor";
    return this;
  }

  public VersionMatchPlugin ceil() {
    this.type = "ceil";
    return this;
  }

  @Override
  public String name() {
    return VersionMatchPlugin.class.getSimpleName();
  }
}
