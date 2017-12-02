package com.github.edgar615.direwolves.plugin.acl;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;

import java.util.List;

/**
 * ACL限制策略.
 * <p>
 * 该插件对应的JSON配置的key为<b>acl.restriction</b>，它有两个属性:
 * <pre>
 *   whitelist 白名单列表
 *   blacklist 黑名单列表
 * </pre>
 * json配置:
 * <pre>
 * "acl.restriction" : {
 *    "whitelist" : ["group1", "group2"],
 *    "blacklist" : ["guest"]
 * }
 * </pre>
 *
 * @author Edgar  Date 2016/9/14
 */
public interface AclRestriction extends ApiPlugin {

  /**
   * 增加白名单.
   * 如果黑名单中存在该group，从黑名单删除.
   * 每个接口最多允许添加100个白名单，超过100个白名单应该采用其他方式。
   *
   * @param group group.
   * @return UserRestriction
   */
  AclRestriction addWhitelist(String group);

  /**
   * 增加黑名单.
   * 如果白名单中存在该group，从白名单中删除.
   * 每个接口最多允许添加100个黑名单，超过100个黑名单应该采用其他方式。
   *
   * @param group group.
   * @return UserRestriction
   */
  AclRestriction addBlacklist(String group);

  /**
   * 删除白名单.
   *
   * @param group group地址.
   * @return UserRestriction
   */
  AclRestriction removeWhitelist(String group);

  /**
   * 删除黑名单.
   *
   * @param group group地址.
   * @return UserRestriction
   */
  AclRestriction removeBlacklist(String group);

  /**
   * 删除所有白名单.
   *
   * @return UserRestriction
   */
  AclRestriction clearWhitelist();

  /**
   * 删除所有黑名单.
   *
   * @return UserRestriction
   */
  AclRestriction clearBlacklist();

  /**
   * @return 白名单列表
   */
  List<String> whitelist();

  /**
   * @return 黑名单列表
   */
  List<String> blacklist();

  @Override
  default String name() {
    return AclRestriction.class.getSimpleName();
  }
}
