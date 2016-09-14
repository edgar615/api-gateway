package com.edgar.direwolves.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * API的IP限制策略.
 * 相同的IP不允许重复出现在黑名单和白名单中。
 * 对于IP的限制策略，按照如下规则处理：
 * 1.检查IP是否在白名单中设置，如果是，则跳过黑名单校验
 * 2.检查IP是否在黑名单中设置，如果是，则拒绝请求
 *
 * @author Edgar  Date 2016/9/12
 */
public class IpRestrictionDefinitionImpl implements IpRestrictionDefinition {

    /**
     * api名称
     */
    private final String apiName;

    /**
     * 白名单
     */
    private final Set<String> whitelist = new HashSet<>();

    /**
     * 黑名单
     */
    private final Set<String> blacklist = new HashSet<>();

    IpRestrictionDefinitionImpl(String apiName) {
        this.apiName = apiName;
    }

    /**
     * 增加白名单.
     * 如果黑名单中存在该IP，从黑名单删除.
     * 每个接口最多允许添加100个白名单，超过100个白名单应该采用其他方式。
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    @Override
    public IpRestrictionDefinition addWhitelist(String ip) {
        Preconditions.checkNotNull(ip, "ip cannot be null");
        Preconditions.checkArgument(whitelist.size() <= 100, "whitelist must <= 100");
        blacklist.remove(ip);
        whitelist.add(ip);
        return this;
    }

    /**
     * 增加黑名单.
     * 如果白名单中存在该IP，从白名单中删除.
     * 每个接口最多允许添加100个黑名单，超过100个黑名单应该采用其他方式。
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    @Override
    public IpRestrictionDefinition addBlacklist(String ip) {
        Preconditions.checkNotNull(ip, "ip cannot be null");
        Preconditions.checkArgument(blacklist.size() <= 100, "blacklist must <= 100");
        whitelist.remove(ip);
        blacklist.add(ip);
        return this;
    }

    /**
     * 删除白名单.
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    @Override
    public IpRestrictionDefinition removeWhitelist(String ip) {
        Preconditions.checkNotNull(ip, "ip cannot be null");
        whitelist.remove(ip);
        return this;
    }

    /**
     * 删除黑名单.
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    @Override
    public IpRestrictionDefinition removeBlacklist(String ip) {
        Preconditions.checkNotNull(ip, "ip cannot be null");
        blacklist.remove(ip);
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("IpRestrictionDefinition")
                .add("apiName", apiName)
                .add("whitelist", whitelist)
                .add("blacklist", blacklist)
                .toString();
    }

    @Override
    public String apiName() {
        return apiName;
    }

    @Override
    public List<String> whitelist() {
        return ImmutableList.copyOf(whitelist);
    }

    @Override
    public List<String> blacklist() {
        return ImmutableList.copyOf(blacklist);
    }
}
