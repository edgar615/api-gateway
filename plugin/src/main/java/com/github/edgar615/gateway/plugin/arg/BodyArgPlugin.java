package com.github.edgar615.gateway.plugin.arg;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

/**
 * 请求体校验的插件.
 * 该插件对应的JSON配置的key为<b>body.arg</b>，它的值是一个json数组，数组中的每个元素包括三个属性:
 * <pre>
 *   name 参数名称
 *   default_value 参数的默认值，如果请求的参数为null，使用默认值代替，默认值null
 *   rules 校验规则，数组，详细格式参考RulesDecoder
 * </pre>
 * json配置:
 * <pre>
 * "body.arg" : [
 * {
 * "name" : "limit",
 * "default_value" : 10,
 * "rules" : {
 * "integer":true,
 * "max":100,
 * "min":1
 * }
 * },
 * {
 * "name" : "start",
 * "default_value" : 0,
 * "rules" : {
 * "integer":true
 * }
 * }
 * ]
 *
 * </pre>
 * Created by edgar on 16-10-22.
 */
public interface BodyArgPlugin extends ApiPlugin, ArgPlugin {

  @Override
  default String name() {
    return BodyArgPlugin.class.getSimpleName();
  }
}
