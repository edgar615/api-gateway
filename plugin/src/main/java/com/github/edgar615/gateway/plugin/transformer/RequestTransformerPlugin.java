package com.github.edgar615.gateway.plugin.transformer;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

import java.util.List;

/**
 * RPC请求参数的转换
 * 该插件对应的JSON配置的key为<b>request.transformer</b>，它的值是一个json数组，数组中的每个元素包括下列属性:
 * <pre>
 *   name endpoint的名称，必填项
 *   header.remove 数组，需要删除的请求头
 *   query.remove 数组，需要删除的请求参数
 *   body.remove 数组，需要删除的请求体
 *   header.replace 数组，需要重命名的请求头，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
 *   query.replace 数组，需要重命名的请求参数，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
 *   body.replace 数组，需要重命名的请求体，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
 *   header.add 数组，需要增加的请求头，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值
 *   query.add 数组，需要增加的请求参数，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值
 *   body.add 数组，需要增加的请求体，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值
 * </pre>
 * json配置:
 * <pre>
 * "request.transformer" : [
 * {
 * "name" : "add_device",
 * "header.remove" : ["h1"],
 * "query.remove" : ["q1"],
 * "body.remove" : ["b1"],
 * "header.replace" : ["h2:r2"],
 * "query.replace" : ["q2:r2"],
 * "body.replace" : ["b2:r2"],
 * "header.add" : ["h2:v2"],
 * "query.add" : ["q2:v2"],
 * "body.add" : ["b2:v2"],
 * }
 * ]
 *
 * </pre>
 * Created by edgar on 16-10-22.
 */
public interface RequestTransformerPlugin extends ApiPlugin {
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

  @Override
  default String name() {
    return RequestTransformerPlugin.class.getSimpleName();
  }
}
