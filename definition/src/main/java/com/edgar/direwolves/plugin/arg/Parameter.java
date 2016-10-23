package com.edgar.direwolves.plugin.arg;

import com.edgar.util.validation.Rule;

import java.util.List;

/**
 * Parameter是一个键值对，其中键是参数名，值对应着参数对配置属性
 * <ul>
 * <li>name 参数的名称，必填项</li>
 * <li>default 默认值，如果参数没有定义默认值，那么默认值用null</li>
 * <li>rule 校验规则，由下列校验规则 required，prohibited，optional，min, max, minLength, maxLength, regex,
 * email, integer, equals</li>
 * </ul>
 *
 * @author Edgar  Date 2016/9/8
 */
public interface Parameter {

  /**
   * 增加一个校验规则.
   *
   * @param rule 校验规则
   * @return
   */
  Parameter addRule(Rule rule);

  /**
   * @return 参数名.
   */
  String name();

  /**
   * @return 默认值.
   */
  Object defaultValue();

  /**
   * @return 校验规则.
   */
  List<Rule> rules();

  static Parameter create(String name, Object defaultValue) {
    return new ParameterImpl(name, defaultValue);
  }
}
