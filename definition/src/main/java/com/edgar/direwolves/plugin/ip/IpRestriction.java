package com.edgar.direwolves.plugin.ip;

import com.edgar.direwolves.plugin.ApiPlugin;

import java.util.List;

/**
 * IP限制策略.
 *
 * @author Edgar  Date 2016/9/14
 */
public interface IpRestriction extends ApiPlugin {

  String NAME = "IP_RESTRICTION";

  /**
   * 增加白名单.
   * 如果黑名单中存在该IP，从黑名单删除.
   * 每个接口最多允许添加100个白名单，超过100个白名单应该采用其他方式。
   *
   * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
   * @return IpRestriction
   */
  IpRestriction addWhitelist(String ip);

  /**
   * 增加黑名单.
   * 如果白名单中存在该IP，从白名单中删除.
   * 每个接口最多允许添加100个黑名单，超过100个黑名单应该采用其他方式。
   *
   * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
   * @return IpRestriction
   */
  IpRestriction addBlacklist(String ip);

  /**
   * 删除白名单.
   *
   * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
   * @return IpRestriction
   */
  IpRestriction removeWhitelist(String ip);

  /**
   * 删除黑名单.
   *
   * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
   * @return IpRestriction
   */
  IpRestriction removeBlacklist(String ip);

  /**
   * 删除所有白名单.
   *
   * @return IpRestriction
   */
  IpRestriction clearWhitelist();

  /**
   * 删除所有黑名单.
   *
   * @return IpRestriction
   */
  IpRestriction clearBlacklist();

  /**
   * @return 白名单列表
   */
  List<String> whitelist();

  /**
   * @return 黑名单列表
   */
  List<String> blacklist();

  default String name() {
    return NAME;
  }
}
