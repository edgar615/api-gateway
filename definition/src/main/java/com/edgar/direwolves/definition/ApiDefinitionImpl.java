package com.edgar.direwolves.definition;

import com.edgar.util.base.MorePreconditions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.vertx.core.http.HttpMethod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * API的路由转发定义.
 * 该类仅定义参数校验，转发规则.对于其他逻辑交由其他的定义类，如<code>AuthDefinition</code>
 * <ul>
 * <li>name 名称，必填项</li>
 * <li>path 路径，可使用正则表达式，必填项</li>
 * <li>method 方法 GET POST PUT DELETE，必填项</li>
 * <li>scope 表示权限范围，默认为default</li>
 * <li>url_arg 查询参数，参考查询参数的定义</li>
 * <li>body_arg body参数，参考body参数的定义</li>
 * <li>description 描述</li>
 * <li>failture_policy 远程调用遇到错误之后对处理策略，默认值fail：直接返回错误信息，如果有多个错误信息，会按照endpont的定义顺序取出第一条信息，origin
 * ：与远程调用对返回值保持一致，custom：自定义对错误信息</li>
 * <li>custom_error:如果failture_policy=custom，该值为必填项，必须满足{code:xxx,message:xxx}的格式</li>
 * <li>endpoints 远程服务对定义，JSON数组，参考Endpoint的定义</li>
 * </ul>
 *
 * @author Edgar  Date 2016/9/8
 */
class ApiDefinitionImpl implements ApiDefinition {

    /**
     * 名称，必填项，全局唯一
     */
    private final String name;

    /**
     * 请求方法 GET | POST | DELETE | PUT.
     */
    private final HttpMethod method;

    /**
     * API路径
     * 示例：/tasks，匹配请求：/tasks.
     * 示例：/tasks，匹配请求：/tasks.
     * 示例：/tasks/([\\d+]+)/abandon，匹配请求/tasks/123/abandon
     */
    private final String path;


    /**
     * 路径的正则表达式.在目前的设计中，它和path保持一致.
     */
    private final Pattern pattern;

//    /**
//     * 描述
//     */
//    private String description;

    /**
     * 权限范围，默认default;
     */
    private final String scope;

    /**
     * URL参数
     */
    private final List<Parameter> urlArgs;

    /**
     * body参数
     */
    private final List<Parameter> bodyArgs;

    /**
     * 是否严格校验参数，如果该值为false，允许传入接口中未定义的参数，如果为true，禁止传入接口中未定义的参数.
     */
    private final boolean strictArg;

    /**
     * 远程请求定义.
     */
    private final List<Endpoint> endpoints;

    /**
     * 过滤器
     */
    private final Set<String> filters = new HashSet<>();

    /**
     * 白名单
     */
    private final Set<String> whitelist = new HashSet<>();

    /**
     * 黑名单
     */
    private final Set<String> blacklist = new HashSet<>();

    private final Set<RateLimit> rateLimits = new HashSet<>();

    public ApiDefinitionImpl(
            ApiDefinitionOption option) {
        this(option.getName(), option.getMethod(), option.getPath(), option.getScope(),
                option.getUrlArgs(), option.getBodyArgs(), option.getEndpoints(), option.isStrictArg());
        this.whitelist.addAll(option.getWhitelist());
        this.blacklist.addAll(option.getBlacklist());
        this.rateLimits.addAll(option.getRateLimits());
        this.filters.addAll(option.getFilters());
    }

    private ApiDefinitionImpl(String name, HttpMethod method, String path, String scope,
                              List<Parameter> urlArgs, List<Parameter> bodyArgs,
                              List<Endpoint> endpoints,
                              boolean strictArg) {
        Preconditions.checkNotNull(name, "name can not be null");
        Preconditions.checkNotNull(method, "method can not be null");
        Preconditions.checkNotNull(path, "path can not be null");
        Preconditions.checkNotNull(scope, "service can not be null");
        Preconditions.checkNotNull(endpoints, "endpoints can not be null");
        MorePreconditions.checkNotEmpty(endpoints, "endpoints can not be empty");
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        this.name = name;
        this.method = method;
        this.path = path;
        this.scope = scope;
        if (urlArgs != null) {
            this.urlArgs = ImmutableList.copyOf(urlArgs);
        } else {
            this.urlArgs = null;
        }
        if (bodyArgs != null) {
            Preconditions.checkArgument(HttpMethod.PUT == method || HttpMethod.POST == method,
                    "can not set body on post|put method");
            this.bodyArgs = ImmutableList.copyOf(bodyArgs);
        } else {
            this.bodyArgs = null;
        }
        this.endpoints = ImmutableList.copyOf(endpoints);
        this.pattern = Pattern.compile(path);
        this.strictArg = strictArg;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public Pattern pattern() {
        return pattern;
    }

    @Override
    public String scope() {
        return scope;
    }

    @Override
    public List<Parameter> urlArgs() {
        return urlArgs;
    }

    @Override
    public List<Parameter> bodyArgs() {
        return bodyArgs;
    }

    @Override
    public List<Endpoint> endpoints() {
        return endpoints;
    }

    @Override
    public List<String> filters() {
        return ImmutableList.copyOf(filters);
    }

    @Override
    public boolean strictArg() {
        return strictArg;
    }

    @Override
    public void addFilter(String filterType) {
        Preconditions.checkNotNull(filterType);
        this.filters.add(filterType);
    }

    @Override
    public void removeFilter(String filterType) {
        Preconditions.checkNotNull(filterType);
        this.filters.remove(filterType);
    }

    /**
     * 增加白名单.
     * 如果黑名单中存在该IP，从黑名单删除.
     * 每个接口最多允许添加100个白名单，超过100个白名单应该采用其他方式。
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    @Override
    public IpRestrictionDefinition addWhitelist(String ip) {
        Preconditions.checkNotNull(ip, "ip cannot be null");
        Preconditions.checkArgument(whitelist.size() <= 100, "whitelist must <= 100");
        blacklist.remove(ip);
        whitelist.add(ip);
        return this;
    }

    /**
     * 增加黑名单.
     * 如果白名单中存在该IP，从白名单中删除.
     * 每个接口最多允许添加100个黑名单，超过100个黑名单应该采用其他方式。
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    @Override
    public IpRestrictionDefinition addBlacklist(String ip) {
        Preconditions.checkNotNull(ip, "ip cannot be null");
        Preconditions.checkArgument(blacklist.size() <= 100, "blacklist must <= 100");
        whitelist.remove(ip);
        blacklist.add(ip);
        return this;
    }

    /**
     * 删除白名单.
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    @Override
    public IpRestrictionDefinition removeWhitelist(String ip) {
        Preconditions.checkNotNull(ip, "ip cannot be null");
        whitelist.remove(ip);
        return this;
    }

    /**
     * 删除黑名单.
     *
     * @param ip ip地址，未做严格校验.允许使用一个完整的IP地址192.168.1.1或者使用通配符192.168.1.*
     * @return IpRestriction
     */
    @Override
    public IpRestrictionDefinition removeBlacklist(String ip) {
        Preconditions.checkNotNull(ip, "ip cannot be null");
        blacklist.remove(ip);
        return this;
    }

    @Override
    public List<String> whitelist() {
        return ImmutableList.copyOf(whitelist);
    }

    @Override
    public List<String> blacklist() {
        return ImmutableList.copyOf(blacklist);
    }

    @Override
    public List<RateLimit> rateLimits() {
        return ImmutableList.copyOf(rateLimits);
    }

    @Override
    public void addRateLimit(RateLimit definition) {
        List<RateLimit> filterDefintions = rateLimits.stream()
                .filter(d -> definition.limitBy().equalsIgnoreCase(d.limitBy())
                        && definition.type().equalsIgnoreCase(d.type()))
                .collect(Collectors.toList());
        rateLimits.add(definition);
        rateLimits.removeAll(filterDefintions);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("ApiDefinition")
                .add("name", name)
                .add("method", method)
                .add("path", path)
                .add("strictArg", strictArg)
                .add("urlArgs", urlArgs)
                .add("bodyArgs", bodyArgs)
                .add("scope", scope)
                .add("endpoints", endpoints)
                .add("filters", filters)
                .add("rateLimits", rateLimits)
                .add("whitelist", whitelist)
                .add("blacklist", blacklist)
                .toString();
    }

    /**
     * 根据组合条件查询映射.
     *
     * @param limitBy 限流分类
     * @param type    限流类型
     */
    @Override
    public void removeRateLimit(String limitBy, String type) {
        Predicate<RateLimit> predicate = rateLimit -> true;
        if (limitBy != null) {
            predicate = predicate.and(rateLimit -> limitBy.equalsIgnoreCase(rateLimit.limitBy()));
        }
        if (type != null) {
            predicate = predicate.and(rateLimit -> type.equalsIgnoreCase(rateLimit.type()));
        }
        this.rateLimits.removeIf(predicate);
    }

}
