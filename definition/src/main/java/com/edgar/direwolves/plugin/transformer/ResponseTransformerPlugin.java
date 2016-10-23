package com.edgar.direwolves.plugin.transformer;

import com.edgar.direwolves.plugin.ApiPlugin;

import java.util.List;

/**
 * Created by edgar on 16-10-22.
 */
public interface ResponseTransformerPlugin extends ApiPlugin {
  default String name() {
    return "RESPONSE_TRANSFORMER";
  }

  /**
   * @return 返回响应的替换规则
   */
  List<ResponseTransformer> transformers();

  /**
   * 增加响应的替换规则
   *
   * @param transformer
   * @return
   */
  ResponseTransformerPlugin addTransformer(ResponseTransformer transformer);

  /**
   * 返回一个响应对替换规则
   *
   * @param name endpoint名称
   * @return
   */
  ResponseTransformer transformer(String name);

  /**
   * 删除响应的替换规则
   *
   * @param name transformer的名称
   * @return
   */
  ResponseTransformerPlugin removeTransformer(String name);

  /**
   * 删除所有的替换规则.
   *
   * @return
   */
  ResponseTransformerPlugin clear();
}
