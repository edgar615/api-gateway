package com.edgar.direwolves.plugin.transformer;

import com.edgar.direwolves.core.definition.ApiPlugin;

import java.util.List;

/**
 * Created by edgar on 16-10-22.
 */
public interface RequestTransformerPlugin extends ApiPlugin {
  String NAME = "REQUEST_TRANSFORMER";
  default String name() {
    return NAME;
  }

  /**
   * @return 返回请求的替换规则
   */
  List<RequestTransformer> transformers();

  /**
   * 增加请求的替换规则
   *
   * @param transformer
   * @return
   */
  RequestTransformerPlugin addTransformer(RequestTransformer transformer);

  /**
   * 返回一个请求对替换规则
   *
   * @param name endpoint名称
   * @return
   */
  RequestTransformer transformer(String name);

  /**
   * 删除请求的替换规则
   *
   * @param name transformer的名称
   * @return
   */
  RequestTransformerPlugin removeTransformer(String name);

  /**
   * 删除所有的替换规则.
   *
   * @return
   */
  RequestTransformerPlugin clear();
}
