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
 * API权限的映射关系的注册表
 */
public class AuthDefinitionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthDefinitionRegistry.class);

    private static final AuthDefinitionRegistry INSTANCE = new AuthDefinitionRegistry();

    private final List<AuthDefinition> definitions = new ArrayList<>();

    public static AuthDefinitionRegistry instance() {
        return INSTANCE;
    }

    /**
     * 获取API权限的映射关系的列表.
     *
     * @return AuthDefinition的不可变集合.
     */
    public Set<AuthDefinition> getDefinitions() {
        return ImmutableSet.copyOf(definitions);
    }

    /**
     * 向注册表中添加一个权限映射.
     * 映射表中apiName和authType的组合必须唯一.重复添加的数据会覆盖掉原来的映射.
     *
     * @param definition 权限映射.
     */
    void add(AuthDefinition definition) {
        Preconditions.checkNotNull(definition, "definition is null");
        remove(definition.getApiName(), definition.getAuthType());
        this.definitions.add(definition);
        LOGGER.debug("add AuthDefinition {}", definition);
    }


    /**
     * 根据组合条件删除映射.
     * 如果apiName=get_device, authType = AuthType.JWT，会从注册表中删除所有apiName=get_device, authType = AuthType.JWT的映射.
     * 如果apiName=get_device, authType = null，会从注册表中删除所有apiName=get_device的映射.
     * 如果apiName=null, authType = AuthType.JWT，会从注册表中删除所有authType = AuthType.JWT的映射.
     *
     * @param apiName  API名称
     * @param authType 权限类型
     */
    void remove(String apiName, AuthType authType) {
        List<AuthDefinition> authDefinitions = filter(apiName, authType);
        if (authDefinitions != null && !authDefinitions.isEmpty()) {
            this.definitions.removeAll(authDefinitions);
            LOGGER.debug("remove authDefinitions {}", authDefinitions);
        }
    }

    /**
     * 根据组合条件查询映射.
     * 如果apiName=get_device, authType = AuthType.JWT，会从注册表中查询apiName=get_device, authType = AuthType.JWT的映射.
     * 如果apiName=get_device, authType = null，会从注册表中查询所有apiName=get_device的映射.
     * 如果apiName=null, authType = AuthType.JWT，会从注册表中查询所有authType = AuthType.JWT的映射.
     *
     * @param apiName  API名称
     * @param authType 权限类型
     * @return AuthDefinition的集合
     */
    List<AuthDefinition> filter(String apiName, AuthType authType) {
        Predicate<AuthDefinition> predicate = definition -> true;
        if (!Strings.isNullOrEmpty(apiName)) {
            predicate = predicate.and(definition -> apiName.equalsIgnoreCase(definition.getApiName()));
        }
        if (authType != null) {
            predicate = predicate.and(definition -> authType == definition.getAuthType());
        }
        return this.definitions.stream().filter(predicate).collect(Collectors.toList());
    }

}