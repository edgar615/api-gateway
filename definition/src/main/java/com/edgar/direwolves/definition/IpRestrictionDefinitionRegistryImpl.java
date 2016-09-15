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
 * IP限制的注册表
 */
public class IpRestrictionDefinitionRegistryImpl implements IpRestrictionDefinitionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpRestrictionDefinitionRegistryImpl.class);

    private static final IpRestrictionDefinitionRegistry INSTANCE = new IpRestrictionDefinitionRegistryImpl();

    private final List<IpRestrictionDefinition> definitions = new ArrayList<>();

    private final Lock rl;
    private final Lock wl;

    private IpRestrictionDefinitionRegistryImpl() {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        this.rl = lock.readLock();
        this.wl = lock.writeLock();
    }

    static IpRestrictionDefinitionRegistry instance() {
        return INSTANCE;
    }

    /**
     * 获取IP限制的列表.
     *
     * @return IpRestrictionDefinition的不可变集合.
     */
    @Override
    public List<IpRestrictionDefinition> getDefinitions() {
        try {
            rl.lock();
            return ImmutableList.copyOf(definitions);
        } finally {
            rl.unlock();
        }
    }

    /**
     * 向注册表中添加一个Ip黑名单限制.
     *
     * @param apiName API名称.
     * @param ip      黑名单IP
     */
    @Override
    public void addBlacklist(String apiName, String ip) {
        Preconditions.checkNotNull(apiName, "apiName is null");
        Preconditions.checkNotNull(ip, "ip is null");
        try {
            wl.lock();
            List<IpRestrictionDefinition> definitions = filter(apiName);
            if (definitions != null && !definitions.isEmpty()) {
                definitions.forEach(definition -> definition.addBlacklist(ip));
            } else {
                IpRestrictionDefinition definition = new IpRestrictionDefinitionImpl(apiName).addBlacklist(ip);
                this.definitions.add(definition);
            }
        } finally {
            wl.unlock();
        }

        LOGGER.debug("add IpRestrictionDefinition apiName->{}, blacklist->{}", apiName, ip);
    }

    /**
     * 向注册表中添加一个Ip白名单限制.
     *
     * @param apiName API名称.
     * @param ip      白名单IP
     */
    @Override
    public void addWhitelist(String apiName, String ip) {
        Preconditions.checkNotNull(apiName, "apiName is null");
        Preconditions.checkNotNull(ip, "ip is null");

        try {
            wl.lock();
            List<IpRestrictionDefinition> definitions = filter(apiName);
            if (definitions != null && !definitions.isEmpty()) {
                definitions.forEach(definition -> definition.addWhitelist(ip));
            } else {
                IpRestrictionDefinition definition = new IpRestrictionDefinitionImpl(apiName).addWhitelist(ip);
                this.definitions.add(definition);
            }
        } finally {
            wl.unlock();
        }

        LOGGER.debug("add IpRestrictionDefinition apiName->{}, whitelist->{}", apiName, ip);
    }

    /**
     * 向注册表中删除一个Ip黑名单限制.
     *
     * @param apiName API名称.
     * @param ip      黑名单IP
     */
    @Override
    public void removeBlacklist(String apiName, String ip) {
        Preconditions.checkNotNull(apiName, "apiName is null");
        Preconditions.checkNotNull(ip, "ip is null");
        try {
            wl.lock();
            List<IpRestrictionDefinition> definitions = filter(apiName);
            if (definitions != null) {
                definitions.forEach(definition -> {
                    definition.removeBlacklist(ip);
                    LOGGER.debug("remove IpRestrictionDefinition apiName->{}, blacklist->{}", apiName, ip);
                });

            }
        } finally {
            wl.unlock();
        }

    }

    /**
     * 向注册表中删除一个Ip白名单限制.
     *
     * @param apiName API名称.
     * @param ip      白名单IP
     */
    @Override
    public void removeWhitelist(String apiName, String ip) {
        Preconditions.checkNotNull(apiName, "apiName is null");
        Preconditions.checkNotNull(ip, "ip is null");

        try {
            wl.lock();
            List<IpRestrictionDefinition> definitions = filter(apiName);
            if (definitions != null) {
                definitions.forEach(definition -> {
                    definition.removeWhitelist(ip);
                    LOGGER.debug("remove IpRestrictionDefinition apiName->{}, whitelist->{}", apiName, ip);
                });

            }
        } finally {
            wl.unlock();
        }

    }

    /**
     * 删除api的全部IP限制策略.
     *
     * @param apiName API名称
     */
    @Override
    public void remove(String apiName) {
        List<IpRestrictionDefinition> definitions = filter(apiName);
        try {
            wl.lock();
            if (definitions != null) {
                definitions.forEach(definition -> {
                    this.definitions.remove(definition);
                    LOGGER.debug("remove IpRestrictionDefinition apiName->{}", definition.apiName());
                });

            }

        } finally {
            wl.unlock();
        }
    }

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
    @Override
    public List<IpRestrictionDefinition> filter(String name) {
        Predicate<IpRestrictionDefinition> predicate = definition -> true;
        predicate = namePredicate(name, predicate);
        List<IpRestrictionDefinition> definitions = null;
        try {
            rl.lock();
            definitions = this.definitions.stream().filter(predicate).collect(Collectors.toList());
        } finally {
            rl.unlock();
        }
        return definitions;
    }

    private Predicate<IpRestrictionDefinition> namePredicate(String name, Predicate<IpRestrictionDefinition> predicate) {
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