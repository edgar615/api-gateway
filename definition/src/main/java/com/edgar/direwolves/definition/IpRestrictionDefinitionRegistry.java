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
     * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
     * *user会查询所有以user结尾对name,如add_user.
     * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
     *
     * @param apiName API名称
     */
    void remove(String apiName);

    /**
     * 根据name查找所有的IP限制.
     * 如果name=null，会查找所有的IP限制.
     * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
     * *user会查询所有以user结尾对name,如add_user.
     * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
     *
     * @param name API名称
     * @return IpRestrictionDefinition
     */
    List<IpRestrictionDefinition> filter(String name);
}
