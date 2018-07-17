package com.github.edgar615.gateway.plugin.version;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

/**
 * API版本的切流
 * .
 * 由于最初的架构并未考虑到多版本适应的方案，为了将代码的改动降低到最低，通过这个插件来实现多版本的共存。
 * 如果某个API需要使用多版本，那么API的定义处理新旧两个版本外，还需要额外定义一个包含了版本选择方案的Api.
 * <p>
 * 在根据请求搜索到多个API后，首先判断有没有版本选择方案，如果没有对应的方案，就抛出异常，
 * 如果找到了方案，就按照方案匹配到一个具体的API
 *
 * @author Edgar  Date 2017/11/6
 */
public class VersionSplitterPlugin implements ApiPlugin {

  /**
   * floor默认取最低版本，ceil默认取最高的版本
   */
  private String unSatisfyStrategy;

  private VersionTraffic traffic;

  public VersionSplitterPlugin() {
  }

  public VersionSplitterPlugin(String unSatisfyStrategy, VersionTraffic traffic) {
    this.unSatisfyStrategy = unSatisfyStrategy;
    this.traffic = traffic;
  }

  public VersionSplitterPlugin floor(VersionTraffic traffic) {
    this.unSatisfyStrategy = "floor";
    this.traffic = traffic;
    return this;
  }

  public VersionSplitterPlugin ceil(VersionTraffic traffic) {
    this.unSatisfyStrategy = "ceil";
    this.traffic = traffic;
    return this;
  }

  public VersionTraffic traffic() {
    return traffic;
  }

  public String unSatisfyStrategy() {
    return unSatisfyStrategy;
  }

  @Override
  public String name() {
    return VersionSplitterPlugin.class.getSimpleName();
  }
}
