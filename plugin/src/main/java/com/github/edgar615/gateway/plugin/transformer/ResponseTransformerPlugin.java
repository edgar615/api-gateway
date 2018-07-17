package com.github.edgar615.gateway.plugin.transformer;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

/**
 * response的转换规则.
 * 该插件对应的JSON配置的key为<b>response.transformer</b>，它的值是一个json对象，包括三个属性:
 * <pre>
 *   header.remove 数组，需要删除的响应头
 *   body.remove 数组，需要删除的响应体
 *   header.replace 数组，需要重命名的响应头，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
 *   body.replace 数组，需要重命名的响应体，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
 *   header.add 数组，需要增加的响应头，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值
 *   body.add 数组，需要增加的响应体，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值
 * </pre>
 * json配置:
 * <pre>
 * "response.transformer" : [
 * {
 * "header.remove" : ["h1"],
 * "body.remove" : ["b1"],
 * "header.replace" : ["h2:r2"],
 * "body.replace" : ["b2:r2"],
 * "header.add" : ["h2:v2"],
 * "body.add" : ["b2:v2"],
 * }
 * ]
 *
 * @author Edgar  Date 2016/10/8
 */
public interface ResponseTransformerPlugin extends BodyTransfomer, HeaderTransfomer, ApiPlugin {
  @Override
  default String name() {
    return ResponseTransformerPlugin.class.getSimpleName();
  }
}
