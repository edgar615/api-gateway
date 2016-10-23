package com.edgar.direwolves.plugin.acl;

import com.edgar.direwolves.plugin.ApiPlugin;

import java.util.List;

/**
 * ACL限制策略.
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
   * @return AclRestriction
   */
  AclRestriction addWhitelist(String group);

  /**
   * 增加黑名单.
   * 如果白名单中存在该group，从白名单中删除.
   * 每个接口最多允许添加100个黑名单，超过100个黑名单应该采用其他方式。
   *
   * @param group group.
   * @return AclRestriction
   */
  AclRestriction addBlacklist(String group);

  /**
   * 删除白名单.
   *
   * @param group group地址.
   * @return AclRestriction
   */
  AclRestriction removeWhitelist(String group);

  /**
   * 删除黑名单.
   *
   * @param group group地址.
   * @return AclRestriction
   */
  AclRestriction removeBlacklist(String group);

  /**
   * 删除所有白名单.
   *
   * @return AclRestriction
   */
  AclRestriction clearWhitelist();

  /**
   * 删除所有黑名单.
   *
   * @return AclRestriction
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

  default String name() {
    return "ACL_RESTRICTION";
  }
}
