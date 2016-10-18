package com.edgar.direwolves.definition;

/**
 * response的转换规则.
 *
 * @author Edgar  Date 2016/10/8
 */
public interface ResponseTransformer extends BodyTransfomer, HeaderTransfomer {

  /**
   * @return endpoint的名称
   */
  String name();

  static ResponseTransformer create(String name) {
    return new ResponseTransformerImpl(name);
  }

}
