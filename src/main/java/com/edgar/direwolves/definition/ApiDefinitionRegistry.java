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
 * 路由映射关系的注册表
 */
public class ApiDefinitionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDefinitionRegistry.class);

    private static final ApiDefinitionRegistry INSTANCE = new ApiDefinitionRegistry();

    private final List<ApiDefinition> definitions = new ArrayList<>();

    public static ApiDefinitionRegistry instance() {
        return INSTANCE;
    }

    /**
     * 获取路由映射的列表.
     *
     * @return ApiMapping的不可变集合.
     */
    public Set<ApiDefinition> getDefinitions() {
        return ImmutableSet.copyOf(definitions);
    }

    /**
     * 向注册表中添加一个路由映射.
     * 映射表中name必须唯一.重复添加的数据会覆盖掉原来的映射.
     *
     * @param apiDefinition 路由映射.
     */
    void add(ApiDefinition apiDefinition) {
        Preconditions.checkNotNull(apiDefinition, "apiDefinition is null");
        remove(apiDefinition.getName());
        this.definitions.add(apiDefinition);
        LOGGER.debug("add ApiDefinition {}", apiDefinition);
    }

    void remove(String name) {
        List<ApiDefinition> apiDefinitions = filter(name);
        if (apiDefinitions != null && !apiDefinitions.isEmpty()) {
            this.definitions.removeAll(apiDefinitions);
            LOGGER.debug("remove ApiDefinition {}", apiDefinitions);
        }
    }

    /**
     * 根据name查找所有的路由映射.
     * 如果name=null，会查找所有的权限映射.
     *
     * @param name API名称
     * @return ApiDefinition的集合
     */
    List<ApiDefinition> filter(String name) {
        Predicate<ApiDefinition> predicate = definition -> true;
        if (!Strings.isNullOrEmpty(name)) {
            predicate = predicate.and(definition -> name.equalsIgnoreCase(definition.getName()));
        }
        return this.definitions.stream().filter(predicate).collect(Collectors.toList());
    }
}