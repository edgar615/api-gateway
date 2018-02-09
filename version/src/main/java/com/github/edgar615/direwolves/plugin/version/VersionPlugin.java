package com.github.edgar615.direwolves.plugin.version;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;

/**
 * Created by Edgar on 2017/11/6.
 *
 * @author Edgar  Date 2017/11/6
 */
public class VersionPlugin implements ApiPlugin {

  private String version;

  @Override
  public String name() {
    return VersionPlugin.class.getSimpleName();
  }

  public String version() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
