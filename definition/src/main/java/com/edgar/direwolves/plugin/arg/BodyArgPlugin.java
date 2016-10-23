package com.edgar.direwolves.plugin.arg;

import com.edgar.direwolves.plugin.ApiPlugin;

/**
 * Created by edgar on 16-10-22.
 */
public interface BodyArgPlugin extends ApiPlugin, ArgPlugin {
  default String name() {
    return "BODY_ARG";
  }

}
