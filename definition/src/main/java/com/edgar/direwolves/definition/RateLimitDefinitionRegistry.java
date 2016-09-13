package com.edgar.direwolves.definition;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * API限流的映射关系的注册表
 */
public class RateLimitDefinitionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitDefinitionRegistry.class);

    private static final RateLimitDefinitionRegistry INSTANCE = new RateLimitDefinitionRegistry();

    private final List<RateLimitDefinition> definitions = new ArrayList<>();

    public static RateLimitDefinitionRegistry instance() {
        return INSTANCE;
    }

    /**
     * 获取API限流的映射关系的列表.
     *
     * @return AuthDefinition的不可变集合.
     */
    public Set<RateLimitDefinition> getDefinitions() {
        return ImmutableSet.copyOf(definitions);
    }

    /**
     * 向注册表中添加一个限流映射.
     * 映射表中apiName，rateLimitBy和rateLimitType的组合必须唯一.重复添加的数据会覆盖掉原来的映射.
     *
     * @param definition 限流映射.
     */
    void add(RateLimitDefinition definition) {
        Preconditions.checkNotNull(definition, "definition is null");
        remove(definition.getApiName(), definition.getRateLimitBy(), definition.getRateLimitType());
        this.definitions.add(definition);
        LOGGER.debug("add RateLimitDefinition {}", definition);
    }

    /**
     * 根据组合条件查询映射.
     * 如果apiName=get_device, rateLimitBy = RateLimitBy.USER，rateLimitType=RateLimitType.SECODE
     * 会从注册表中删除apiName=get_device, rateLimitBy = RateLimitBy.USER，rateLimitType=RateLimitType.SECODE的映射.
     * 如果apiName=get_device, rateLimitBy = null，rateLimitType=RateLimitType.SECODE
     * 会从注册表中删除apiName=get_device, rateLimitType=RateLimitType.SECODE的映射.
     * 如果apiName=null, rateLimitBy = RateLimitBy.USER，rateLimitType=RateLimitType.SECODE
     * 会从注册表中删除rateLimitBy = RateLimitBy.USER，rateLimitType=RateLimitType.SECODE的映射.
     *
     * @param apiName       API名称
     * @param rateLimitBy   限流分类
     * @param rateLimitType 限流类型
     */
    void remove(String apiName, RateLimitBy rateLimitBy, RateLimitType rateLimitType) {
        List<RateLimitDefinition> rateLimitDefinitions = filter(apiName, rateLimitBy, rateLimitType);
        if (rateLimitDefinitions != null && !rateLimitDefinitions.isEmpty()) {
            this.definitions.removeAll(rateLimitDefinitions);
            LOGGER.debug("remove RateLimitDefinition {}", rateLimitDefinitions);
        }
    }

    /**
     * 根据组合条件查询映射.
     * 如果apiName=get_device, rateLimitBy = RateLimitBy.USER，rateLimitType=RateLimitType.SECODE
     * 会从注册表中查询apiName=get_device, rateLimitBy = RateLimitBy.USER，rateLimitType=RateLimitType.SECODE的映射.
     * 如果apiName=get_device, rateLimitBy = null，rateLimitType=RateLimitType.SECODE
     * 会从注册表中查询apiName=get_device, rateLimitType=RateLimitType.SECODE的映射.
     * 如果apiName=null, rateLimitBy = RateLimitBy.USER，rateLimitType=RateLimitType.SECODE
     * 会从注册表中查询rateLimitBy = RateLimitBy.USER，rateLimitType=RateLimitType.SECODE的映射.
     *
     * @param apiName       API名称
     * @param rateLimitBy   限流分类
     * @param rateLimitType 限流类型
     * @return RateLimitDefinition的集合
     */
    List<RateLimitDefinition> filter(String apiName, RateLimitBy rateLimitBy, RateLimitType rateLimitType) {
        Predicate<RateLimitDefinition> predicate = definition -> true;
        if (!Strings.isNullOrEmpty(apiName)) {
            predicate = predicate.and(definition -> apiName.equalsIgnoreCase(definition.getApiName()));
        }
        if (rateLimitBy != null) {
            predicate = predicate.and(definition -> rateLimitBy == definition.getRateLimitBy());
        }
        if (rateLimitType != null) {
            predicate = predicate.and(definition -> rateLimitType == definition.getRateLimitType());
        }
        return this.definitions.stream().filter(predicate).collect(Collectors.toList());
    }
}