package com.edgar.direwolves.plugin.transformer;

/**
 * response的转换规则.
 *
 * @author Edgar  Date 2016/10/8
 */
public interface ResponseTransformer extends BodyTransfomer, HeaderTransfomer {

  static ResponseTransformer create(String name) {
    return new ResponseTransformerImpl(name);
  }

  /**
   * @return endpoint的名称
   */
  String name();

}
