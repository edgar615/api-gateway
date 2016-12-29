package com.edgar.direwolves.plugin.arg;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * Created by Edgar on 2016/12/29.
 *
 * @author Edgar  Date 2016/12/29
 */
public interface StrictArgPlugin extends ApiPlugin {
  @Override
  default String name() {
    return StrictArgPlugin.class.getSimpleName();
  }
}
