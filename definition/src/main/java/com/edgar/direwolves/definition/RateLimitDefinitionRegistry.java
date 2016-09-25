package com.edgar.direwolves.definition;

import java.util.List;

/**
 * API限流的映射关系的注册表.
 * Created by edgar on 16-9-14.
 */
public interface RateLimitDefinitionRegistry {

    static RateLimitDefinitionRegistry create() {
        return RateLimitDefinitionRegistryImpl.instance();
    }

    /**
     * 获取API限流的映射关系的列表.
     *
     * @return AuthDefinition的不可变集合.
     */
    List<RateLimitDefinition> getDefinitions();

    /**
     * 向注册表中添加一个限流映射.
     * 映射表中apiName，rateLimitBy和rateLimitType的组合必须唯一.重复添加的数据会覆盖掉原来的映射.
     *
     * @param definition 限流映射.
     */
    void add(RateLimitDefinition definition);

    /**
     * 根据组合条件查询映射.
     * 如果apiName=get_device, rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE
     * 会从注册表中删除apiName=get_device, rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE的映射.
     * 如果apiName=get_device, rateLimitBy = null，type=RateLimitType.SECODE
     * 会从注册表中删除apiName=get_device, type=RateLimitType.SECODE的映射.
     * 如果apiName=null, rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE
     * 会从注册表中删除rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE的映射.
     * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
     * *user会查询所有以user结尾对name,如add_user.
     * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
     *
     * @param apiName       API名称
     * @param rateLimitBy   限流分类
     * @param rateLimitType 限流类型
     */
    void remove(String apiName, RateLimitBy rateLimitBy, RateLimitType rateLimitType);

    /**
     * 根据组合条件查询映射.
     * 如果apiName=get_device, rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE
     * 会从注册表中删除apiName=get_device, rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE的映射.
     * 如果apiName=get_device, rateLimitBy = null，type=RateLimitType.SECODE
     * 会从注册表中删除apiName=get_device, type=RateLimitType.SECODE的映射.
     * 如果apiName=null, rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE
     * 会从注册表中删除rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE的映射.
     *
     * @param apiName       API名称
     * @param rateLimitBy   限流分类
     * @param rateLimitType 限流类型
     */
    List<RateLimitDefinition> filter(String apiName, RateLimitBy rateLimitBy, RateLimitType rateLimitType);
}
