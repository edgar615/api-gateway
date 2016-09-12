package com.edgar.direwolves.definition;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
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
public class IpRestrictionDefinitionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpRestrictionDefinitionRegistry.class);

    private static final IpRestrictionDefinitionRegistry INSTANCE = new IpRestrictionDefinitionRegistry();

    private final List<IpRestrictionDefinition> definitions = new ArrayList<>();

    public static IpRestrictionDefinitionRegistry instance() {
        return INSTANCE;
    }

    /**
     * 获取IP限制的列表.
     *
     * @return IpRestrictionDefinition的不可变集合.
     */
    public List<IpRestrictionDefinition> getDefinitions() {
        return ImmutableList.copyOf(definitions);
    }

    /**
     * 向注册表中添加一个Ip黑名单限制.
     *
     * @param apiName API名称.
     * @param ip      黑名单IP
     */
    void addBlacklist(String apiName, String ip) {
        Preconditions.checkNotNull(apiName, "apiName is null");
        Preconditions.checkNotNull(ip, "ip is null");
        IpRestrictionDefinition definition = filter(apiName);
        if (definition != null) {
            definition.addBlacklist(ip);
        } else {
            definition = new IpRestrictionDefinition(apiName).addBlacklist(ip);
            definitions.add(definition);
        }
        LOGGER.debug("add IpRestrictionDefinition apiName->{}, blacklist->{}", apiName, ip);
    }

    /**
     * 向注册表中添加一个Ip白名单限制.
     *
     * @param apiName API名称.
     * @param ip      白名单IP
     */
    void addWhitelist(String apiName, String ip) {
        Preconditions.checkNotNull(apiName, "apiName is null");
        Preconditions.checkNotNull(ip, "ip is null");
        IpRestrictionDefinition definition = filter(apiName);
        if (definition != null) {
            definition.addWhitelist(ip);
        } else {
            definition = new IpRestrictionDefinition(apiName).addWhitelist(ip);
            definitions.add(definition);
        }
        LOGGER.debug("add IpRestrictionDefinition apiName->{}, whitelist->{}", apiName, ip);
    }

    /**
     * 向注册表中删除一个Ip黑名单限制.
     *
     * @param apiName API名称.
     * @param ip      黑名单IP
     */
    void removeBlacklist(String apiName, String ip) {
        Preconditions.checkNotNull(apiName, "apiName is null");
        Preconditions.checkNotNull(ip, "ip is null");
        IpRestrictionDefinition definition = filter(apiName);
        if (definition != null) {
            definition.removeBlacklist(ip);
            LOGGER.debug("remove IpRestrictionDefinition apiName->{}, blacklist->{}", apiName, ip);
        }
    }

    /**
     * 向注册表中删除一个Ip白名单限制.
     *
     * @param apiName API名称.
     * @param ip      白名单IP
     */
    void removeWhitelist(String apiName, String ip) {
        Preconditions.checkNotNull(apiName, "apiName is null");
        Preconditions.checkNotNull(ip, "ip is null");
        IpRestrictionDefinition definition = filter(apiName);
        if (definition != null) {
            definition.removeWhitelist(ip);
            LOGGER.debug("remove IpRestrictionDefinition apiName->{}, whitelist->{}", apiName, ip);
        }
    }

    /**
     * 删除api的全部IP限制策略.
     *
     * @param apiName API名称
     */
    void remove(String apiName) {
        IpRestrictionDefinition definition = filter(apiName);
        if (definition != null) {
            this.definitions.remove(definition);
            LOGGER.debug("remove ApiDefinition {}", definition);
        }
    }

    /**
     * 根据name查找所有的路由映射.
     * 如果name=null，会查找所有的权限映射.
     *
     * @param name API名称
     * @return IpRestrictionDefinition
     */
    IpRestrictionDefinition filter(String name) {
        Predicate<IpRestrictionDefinition> predicate = definition -> true;
        if (!Strings.isNullOrEmpty(name)) {
            predicate = predicate.and(definition -> name.equalsIgnoreCase(definition.getApiName()));
        }
        List<IpRestrictionDefinition> definitions = this.definitions.stream().filter(predicate).collect(Collectors.toList());
        if (definitions == null || definitions.isEmpty()) {
            return null;
        }
        return definitions.get(0);
    }
}