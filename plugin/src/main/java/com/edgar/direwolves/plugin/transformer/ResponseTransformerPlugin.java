package com.edgar.direwolves.plugin.transformer;

import com.edgar.direwolves.core.definition.ApiPlugin;

/**
 * response的转换规则.
 *
 * @author Edgar  Date 2016/10/8
 */
public interface ResponseTransformerPlugin extends BodyTransfomer, HeaderTransfomer, ApiPlugin {
  @Override
  default String name() {
    return ResponseTransformerPlugin.class.getSimpleName();
  }
}
