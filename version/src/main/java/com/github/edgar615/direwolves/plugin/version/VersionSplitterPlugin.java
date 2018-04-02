package com.github.edgar615.direwolves.plugin.version;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;

import java.util.ArrayList;
import java.util.List;

public class VersionSplitterPlugin implements ApiPlugin {

  /**
   * floor默认取最低版本，ceil默认取最高的版本
   */
  private String unSatisfyStrategy ;

  private final List<IpSplitPolicy> policies = new ArrayList<>();

  public List<IpSplitPolicy> policies() {
    return policies;
  }

  public String unSatisfyStrategy() {
    return unSatisfyStrategy;
  }

  public void setUnSatisfyStrategy(String unSatisfyStrategy) {
    this.unSatisfyStrategy = unSatisfyStrategy;
  }

  public VersionSplitterPlugin setPolicies(List<IpSplitPolicy> policies) {
    this.policies.clear();
    this.policies.addAll(policies);
    return this;
  }

  @Override
  public String name() {
    return VersionSplitterPlugin.class.getSimpleName();
  }
}
