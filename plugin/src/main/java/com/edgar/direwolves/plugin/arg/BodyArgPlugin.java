package com.edgar.direwolves.plugin.arg;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * Created by edgar on 16-10-22.
 */
public interface BodyArgPlugin extends ApiPlugin, ArgPlugin {
  String NAME = "BODY_ARG";

  default String name() {
    return NAME;
  }

}
