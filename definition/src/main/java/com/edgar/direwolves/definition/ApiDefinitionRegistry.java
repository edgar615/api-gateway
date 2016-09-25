package com.edgar.direwolves.definition;

import java.util.List;
import java.util.Set;

/**
 * 路由映射关系的注册表.
 * Created by edgar on 16-9-13.
 */
public interface ApiDefinitionRegistry {

    static ApiDefinitionRegistry create() {
        return ApiDefinitionRegistryImpl.instance();
    }

    /**
     * 获取路由映射的列表.
     *
     * @return ApiMapping的不可变集合.
     */
    Set<ApiDefinition> getDefinitions();

    /**
     * 向注册表中添加一个路由映射.
     * 映射表中name必须唯一.重复添加的数据会覆盖掉原来的映射.
     *
     * @param apiDefinition 路由映射.
     */
    void add(ApiDefinition apiDefinition);

    /**
     * 根据name删除符合的路由映射.
     * 如果name=null，会查找所有的权限映射.
     * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
     * *user会查询所有以user结尾对name,如add_user.
     * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
     *
     * @param name API名称
     */
    void remove(String name);

    /**
     * 根据name查找所有的路由映射.
     * 如果name=null，会查找所有的权限映射.
     * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
     * *user会查询所有以user结尾对name,如add_user.
     * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
     *
     * @param name API名称
     * @return ApiDefinition的集合
     */
    List<ApiDefinition> filter(String name);

    /**
     * 给api增加一个filter
     *
     * @param filterType
     */
    void addFilter(String name, String filterType);

    /**
     * 给api删除一个filter
     * @param filterType
     */
    void removeFilter(String name, String filterType);

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
}
