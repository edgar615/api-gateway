package com.edgar.direvolves.plugin.authentication;

import com.google.common.base.MoreObjects;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * 隐藏的一个插件，主要用于超级管理员的访问授权..
 *
 * @author Edgar  Date 2016/10/31
 */
public class BackdoorVertifyPlugin implements ApiPlugin {

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper("BackdoorVertifyPlugin")
            .toString();
  }

  @Override
  public String name() {
    return BackdoorVertifyPlugin.class.getSimpleName();
  }
}
