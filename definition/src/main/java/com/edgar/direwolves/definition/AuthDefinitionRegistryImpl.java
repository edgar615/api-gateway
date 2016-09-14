package com.edgar.direwolves.definition;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * API权限的映射关系的注册表
 */
class AuthDefinitionRegistryImpl implements AuthDefinitionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthDefinitionRegistryImpl.class);
    private static final AuthDefinitionRegistry INSTANCE = new AuthDefinitionRegistryImpl();
    private final List<AuthDefinition> definitions = new ArrayList<>();
    private final Lock rl;
    private final Lock wl;

    private AuthDefinitionRegistryImpl() {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        this.rl = lock.readLock();
        this.wl = lock.writeLock();
    }

    static AuthDefinitionRegistry instance() {
        return INSTANCE;
    }

    /**
     * 获取API权限的映射关系的列表.
     *
     * @return AuthDefinition的不可变集合.
     */
    @Override
    public Set<AuthDefinition> getDefinitions() {
        try {
            rl.lock();
            return ImmutableSet.copyOf(definitions);
        } finally {
            rl.unlock();
        }
    }

    /**
     * 向注册表中添加一个权限映射.
     * 映射表中apiName和authType的组合必须唯一.重复添加的数据会覆盖掉原来的映射.
     *
     * @param definition 权限映射.
     */
    @Override
    public void add(AuthDefinition definition) {
        Preconditions.checkNotNull(definition, "definition is null");
        try {
            wl.lock();
            remove(definition.apiName(), definition.authType());
            this.definitions.add(definition);
        } finally {
            wl.unlock();
        }
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
    @Override
    public void remove(String apiName, AuthType authType) {
        List<AuthDefinition> authDefinitions = filter(apiName, authType);
        if (authDefinitions != null && !authDefinitions.isEmpty()) {
            try {
                wl.lock();
                this.definitions.removeAll(authDefinitions);
            } finally {
                wl.unlock();
            }
            LOGGER.debug("remove authDefinitions {}", authDefinitions);
        }
    }

    /**
     * 根据组合条件查询映射.
     * 如果apiName=get_device, authType = AuthType.JWT，会从注册表中查询apiName=get_device, authType = AuthType.JWT的映射.
     * 如果apiName=get_device, authType = null，会从注册表中查询所有apiName=get_device的映射.
     * 如果apiName=null, authType = AuthType.JWT，会从注册表中查询所有authType = AuthType.JWT的映射.
     * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
     * *user会查询所有以user结尾对name,如add_user.
     * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
     *
     * @param apiName  API名称
     * @param authType 权限类型
     * @return AuthDefinition的集合
     */
    @Override
    public List<AuthDefinition> filter(String apiName, AuthType authType) {
        Predicate<AuthDefinition> predicate = definition -> true;
        predicate = namePredicate(apiName, predicate);
        if (authType != null) {
            predicate = predicate.and(definition -> authType == definition.authType());
        }
        try {
            rl.lock();
            return this.definitions.stream().filter(predicate).collect(Collectors.toList());
        } finally {
            rl.unlock();
        }
    }

    private Predicate<AuthDefinition> namePredicate(String name, Predicate<AuthDefinition> predicate) {
        if (!Strings.isNullOrEmpty(name) && !"*".equals(name)) {
            boolean isStartsWith = false;
            boolean isEndsWith = false;
            String checkName = name;
            if (name.endsWith("*")) {
                checkName = checkName.substring(0, name.length() - 1);
                isStartsWith = true;
            }
            if (name.startsWith("*")) {
                checkName = checkName.substring(1);
                isEndsWith = true;
            }
            final String finalCheckName = checkName.toLowerCase();
            final boolean finalIsStartsWith = isStartsWith;
            final boolean finalIsEndsWith = isEndsWith;
            predicate = predicate.and(definition -> {
                boolean startCheck = false;
                boolean endCheck = false;
                boolean equalsCheck = false;
                if (finalIsStartsWith) {
                    startCheck = definition.apiName().toLowerCase().startsWith(finalCheckName);
                }
                if (finalIsEndsWith) {
                    endCheck = definition.apiName().toLowerCase().endsWith(finalCheckName);
                }
                equalsCheck = finalCheckName.equalsIgnoreCase(definition.apiName());
                return equalsCheck || endCheck || startCheck;
            });
        }
        return predicate;
    }

}