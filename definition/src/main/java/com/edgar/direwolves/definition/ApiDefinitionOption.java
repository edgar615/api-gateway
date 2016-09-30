package com.edgar.direwolves.definition;

import io.vertx.core.http.HttpMethod;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ApiDefinitionOption {

  private static final String SCOPE = "default";

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

  /**
   * 服务名
   */
  private String name;

  /**
   * 请求方法 GET | POST | DELETE | PUT
   */
  private HttpMethod method = HttpMethod.GET;

  /**
   * 远程rest路径
   * 示例：/tasks
   * 示例：/tasks/$1/abandon，$1表示当前请求上下文中的$1变量
   */
  private String path;

  /**
   * 权限范围，默认default;
   */
  private String scope = SCOPE;

  /**
   * URL参数
   */
  private  List<Parameter> urlArgs;

  /**
   * body参数
   */
  private List<Parameter> bodyArgs;

  /**
   * 远程请求定义.
   */
  private List<Endpoint> endpoints;

  /**
   * 是否严格校验参数，如果该值为false，允许传入接口中未定义的参数，如果为true，禁止传入接口中未定义的参数.
   */
  private boolean strictArg;

  ApiDefinitionOption() {
  }

  public boolean isStrictArg() {
    return strictArg;
  }

  public ApiDefinitionOption setStrictArg(boolean strictArg) {
    this.strictArg = strictArg;
    return this;
  }

  public String getName() {
    return name;
  }

  public ApiDefinitionOption setName(String name) {
    this.name = name;
    return this;
  }

  public HttpMethod getMethod() {
    return method;
  }

  public ApiDefinitionOption setMethod(HttpMethod method) {
    this.method = method;
    return this;
  }

  public String getPath() {
    return path;
  }

  public ApiDefinitionOption setPath(String path) {
    this.path = path;
    return this;
  }

  public String getScope() {
    return scope;
  }

  public ApiDefinitionOption setScope(String scope) {
    this.scope = scope;
    return this;
  }

  public List<Parameter> getUrlArgs() {
    return urlArgs;
  }

  public ApiDefinitionOption setUrlArgs(List<Parameter> urlArgs) {
    this.urlArgs = urlArgs;
    return this;
  }

  public List<Parameter> getBodyArgs() {
    return bodyArgs;
  }

  public ApiDefinitionOption setBodyArgs(List<Parameter> bodyArgs) {
    this.bodyArgs = bodyArgs;
    return this;
  }

  public List<Endpoint> getEndpoints() {
    return endpoints;
  }

  public ApiDefinitionOption setEndpoints(List<Endpoint> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  public Set<String> getFilters() {
    return filters;
  }

  public Set<String> getWhitelist() {
    return whitelist;
  }

  public Set<String> getBlacklist() {
    return blacklist;
  }

  public Set<RateLimit> getRateLimits() {
    return rateLimits;
  }

  public ApiDefinitionOption addFilter(String filterType) {
    filters.add(filterType);
    return this;
  }

  public ApiDefinitionOption addFilters(Collection<String> filterTypes) {
    filters.addAll(filterTypes);
    return this;
  }

  public ApiDefinitionOption addWhitelist(String ip) {
    whitelist.add(ip);
    return this;
  }

  public ApiDefinitionOption addWhitelist(Collection<String> ipList) {
    whitelist.addAll(ipList);
    return this;
  }

  public ApiDefinitionOption addBlacklist(String ip) {
    blacklist.add(ip);
    return this;
  }

  public ApiDefinitionOption addBlacklist(Collection<String> ipList) {
    blacklist.addAll(ipList);
    return this;
  }

  public ApiDefinitionOption addRateLimit(RateLimit rateLimit) {
    rateLimits.add(rateLimit);
    return this;
  }

  public ApiDefinitionOption addRateLimit(Collection<RateLimit> rateLimits) {
    rateLimits.addAll(rateLimits);
    return this;
  }
}