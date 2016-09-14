package com.edgar.direwolves.definition;

import java.util.List;

/**
 * IP限制的注册表.
 *
 * @author Edgar  Date 2016/9/14
 */
public interface IpRestrictionDefinitionRegistry {

    static IpRestrictionDefinitionRegistry create() {
        return IpRestrictionDefinitionRegistryImpl.instance();
    }

    /**
     * 获取IP限制的列表.
     *
     * @return IpRestrictionDefinition的不可变集合.
     */
    List<IpRestrictionDefinition> getDefinitions();

    /**
     * 向注册表中添加一个Ip黑名单限制.
     *
     * @param apiName API名称.
     * @param ip      黑名单IP
     */
    void addBlacklist(String apiName, String ip);

    /**
     * 向注册表中添加一个Ip白名单限制.
     *
     * @param apiName API名称.
     * @param ip      白名单IP
     */
    void addWhitelist(String apiName, String ip);

    /**
     * 向注册表中删除一个Ip黑名单限制.
     *
     * @param apiName API名称.
     * @param ip      黑名单IP
     */
    void removeBlacklist(String apiName, String ip);

    /**
     * 向注册表中删除一个Ip白名单限制.
     *
     * @param apiName API名称.
     * @param ip      白名单IP
     */
    void removeWhitelist(String apiName, String ip);

    /**
     * 根据名称删除api的IP限制策略.
     *
     * @param apiName API名称
     */
    void remove(String apiName);

    /**
     * 根据name查找所有的IP限制.
     * 如果name=null，会查找所有的IP限制.
     *
     * @param name API名称
     * @return IpRestrictionDefinition
     */
    IpRestrictionDefinition filter(String name);
}
