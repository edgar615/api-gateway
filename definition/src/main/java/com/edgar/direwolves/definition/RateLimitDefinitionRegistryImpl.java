package com.edgar.direwolves.definition;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * API限流的映射关系的注册表.
 */
class RateLimitDefinitionRegistryImpl implements RateLimitDefinitionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitDefinitionRegistryImpl.class);

    private static final RateLimitDefinitionRegistry INSTANCE = new RateLimitDefinitionRegistryImpl();

    private final List<RateLimitDefinition> definitions = new ArrayList<>();
    private final Lock rl;
    private final Lock wl;

    private RateLimitDefinitionRegistryImpl() {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        this.rl = lock.readLock();
        this.wl = lock.writeLock();
    }

    public static RateLimitDefinitionRegistry instance() {
        return INSTANCE;
    }

    /**
     * 获取API限流的映射关系的列表.
     *
     * @return AuthDefinition的不可变集合.
     */
    @Override
    public List<RateLimitDefinition> getDefinitions() {
        try {
            rl.lock();
            return ImmutableList.copyOf(definitions);
        } finally {
            rl.unlock();
        }
    }

    /**
     * 向注册表中添加一个限流映射.
     * 映射表中apiName，rateLimitBy和rateLimitType的组合必须唯一.重复添加的数据会覆盖掉原来的映射.
     *
     * @param definition 限流映射.
     */
    @Override
    public void add(RateLimitDefinition definition) {
        Preconditions.checkNotNull(definition, "definition is null");

        try {
            wl.lock();
            remove(definition.apiName(), definition.rateLimitBy(), definition.type());
            this.definitions.add(definition);
        } finally {
            wl.unlock();
        }

        LOGGER.debug("add RateLimitDefinition {}", definition);
    }

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
    @Override
    public void remove(String apiName, RateLimitBy rateLimitBy, RateLimitType rateLimitType) {
        List<RateLimitDefinition> rateLimitDefinitions = filter(apiName, rateLimitBy, rateLimitType);
        if (rateLimitDefinitions != null && !rateLimitDefinitions.isEmpty()) {
            try {
                wl.lock();
                this.definitions.removeAll(rateLimitDefinitions);
                LOGGER.debug("remove RateLimitDefinition {}", rateLimitDefinitions);
            } finally {
                wl.unlock();
            }
        }
    }

    /**
     * 根据组合条件查询映射.
     * 如果apiName=get_device, rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE
     * 会从注册表中查询apiName=get_device, rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE的映射.
     * 如果apiName=get_device, rateLimitBy = null，type=RateLimitType.SECODE
     * 会从注册表中查询apiName=get_device, type=RateLimitType.SECODE的映射.
     * 如果apiName=null, rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE
     * 会从注册表中查询rateLimitBy = RateLimitBy.USER，type=RateLimitType.SECODE的映射.
     * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
     * *user会查询所有以user结尾对name,如add_user.
     * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
     *
     * @param apiName       API名称
     * @param rateLimitBy   限流分类
     * @param rateLimitType 限流类型
     * @return RateLimitDefinition的集合
     */
    @Override
    public List<RateLimitDefinition> filter(String apiName, RateLimitBy rateLimitBy, RateLimitType rateLimitType) {
        Predicate<RateLimitDefinition> predicate = definition -> true;
        predicate = namePredicate(apiName, predicate);
        if (rateLimitBy != null) {
            predicate = predicate.and(definition -> rateLimitBy == definition.rateLimitBy());
        }
        if (rateLimitType != null) {
            predicate = predicate.and(definition -> rateLimitType == definition.type());
        }
        try {
            rl.lock();
            return this.definitions.stream().filter(predicate).collect(Collectors.toList());
        } finally {
            rl.unlock();
        }
    }

    private Predicate<RateLimitDefinition> namePredicate(String name, Predicate<RateLimitDefinition> predicate) {
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