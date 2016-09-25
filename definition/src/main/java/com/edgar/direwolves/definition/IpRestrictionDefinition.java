package com.edgar.direwolves.definition;

import java.util.List;

/**
 * Created by Edgar on 2016/9/14.
 *
 * @author Edgar  Date 2016/9/14
 */
public interface IpRestrictionDefinition {

    /**
     * 增加白名单.
     * 如果黑名单中存在该IP，从黑名单删除.
     * 每个接口最多允许添加100个白名单，超过100个白名单应该采用其他方式。
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    IpRestrictionDefinition addWhitelist(String ip);

    /**
     * 增加黑名单.
     * 如果白名单中存在该IP，从白名单中删除.
     * 每个接口最多允许添加100个黑名单，超过100个黑名单应该采用其他方式。
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    IpRestrictionDefinition addBlacklist(String ip);

    /**
     * 删除白名单.
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    IpRestrictionDefinition removeWhitelist(String ip);

    /**
     * 删除黑名单.
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    IpRestrictionDefinition removeBlacklist(String ip);

    /**
     *
     * @return 白名单列表
     */
    List<String> whitelist();

    /**
     *
     * @return 黑名单列表
     */
    List<String> blacklist();
}
