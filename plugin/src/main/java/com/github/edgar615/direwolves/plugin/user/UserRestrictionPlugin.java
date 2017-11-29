package com.github.edgar615.direwolves.plugin.user;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;

import java.util.List;

/**
 * User限制策略.
 * <p>
 * 该插件对应的JSON配置的key为<b>user.restriction</b>，它有两个属性:
 * <pre>
 *   whitelist 白名单列表
 *   blacklist 黑名单列表
 * </pre>
 * json配置:
 * <pre>
 * "user.restriction" : {
 *    "whitelist" : [1, 2],
 *    "blacklist" : ["*"]
 * }
 * </pre>
 *
 * @author Edgar  Date 2016/9/14
 */
public interface UserRestrictionPlugin extends ApiPlugin {

  /**
   * 增加白名单.
   * 如果黑名单中存在该userId，从黑名单删除.
   * 每个接口最多允许添加100个白名单，超过100个白名单应该采用其他方式。
   *
   * @param userId userId.
   * @return UserRestrictionPlugin
   */
  UserRestrictionPlugin addWhitelist(String userId);

  /**
   * 增加黑名单.
   * 如果白名单中存在该userId，从白名单中删除.
   * 每个接口最多允许添加100个黑名单，超过100个黑名单应该采用其他方式。
   *
   * @param userId userId.
   * @return UserRestrictionPlugin
   */
  UserRestrictionPlugin addBlacklist(String userId);

  /**
   * 删除白名单.
   *
   * @param userId userId.
   * @return UserRestrictionPlugin
   */
  UserRestrictionPlugin removeWhitelist(String userId);

  /**
   * 删除黑名单.
   *
   * @param userId userId.
   * @return UserRestrictionPlugin
   */
  UserRestrictionPlugin removeBlacklist(String userId);

  /**
   * 删除所有白名单.
   *
   * @return UserRestrictionPlugin
   */
  UserRestrictionPlugin clearWhitelist();

  /**
   * 删除所有黑名单.
   *
   * @return UserRestrictionPlugin
   */
  UserRestrictionPlugin clearBlacklist();

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
    return UserRestrictionPlugin.class.getSimpleName();
  }
}
