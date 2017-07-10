package com.edgar.direwolves.plugin.ip;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Log;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IP限制的filter.
 * 该filter从API上下文中读取读取调用方的IP，<code>request
 * .client_ip</code>变量，如果这个IP属于白名单，直接允许访问（不在考虑黑名单）；如果这个IP属于黑名单，直接返回1004的错误.
 * <p>
 * <p>
 * 该filter的order=5
 * <p>
 * 该filter可以接受下列的配置参数
 * <pre>
 *   ip.blacklist 全局的黑名单
 *  ip.whitelist 全局的白名单
 * </pre>
 * Created by edgar on 16-12-24.
 */
public class IpRestrictionFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(IpRestrictionFilter.class);

  private final List<String> globalBlacklist = new ArrayList<>();

  private final List<String> globalWhitelist = new ArrayList<>();


  public IpRestrictionFilter(JsonObject config) {
    JsonObject jsonObject = config.getJsonObject("ip.restriction", new JsonObject());
    JsonArray blackArray = jsonObject.getJsonArray("blacklist", new JsonArray());
    JsonArray whiteArray = jsonObject.getJsonArray("whitelist", new JsonArray());
    for (int i = 0; i < blackArray.size(); i++) {
      globalBlacklist.add(blackArray.getString(i));
    }
    for (int i = 0; i < whiteArray.size(); i++) {
      globalWhitelist.add(whiteArray.getString(i));
    }
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 5;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return !globalBlacklist.isEmpty()
           || !globalWhitelist.isEmpty()
           || apiContext.apiDefinition().plugin(IpRestriction.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    IpRestriction plugin =
            (IpRestriction) apiContext.apiDefinition().plugin(IpRestriction.class.getSimpleName());
    List<String> blacklist = new ArrayList<>(globalBlacklist);
    List<String> whitelist = new ArrayList<>(globalWhitelist);
    if (plugin != null) {
      blacklist.addAll(plugin.blacklist());
      whitelist.addAll(plugin.whitelist());
    }
    String clientIp = (String) apiContext.variables().get("request.client_ip");
    List<String> black = blacklist.stream()
            .filter(r -> checkIp(r, clientIp))
            .collect(Collectors.toList());
    List<String> white = whitelist.stream()
            .filter(r -> checkIp(r, clientIp))
            .collect(Collectors.toList());
    if (white.isEmpty() && !black.isEmpty()) {
      Log.create(LOGGER)
              .setTraceId(apiContext.id())
              .setEvent("ip.restriction.tripped")
              .warn();
      completeFuture.fail(SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
                                  .set("details", "The ip is forbidden"));
    } else {
      completeFuture.complete(apiContext);
    }
  }

  private boolean checkIp(String rule, String clientIp) {
    List<String> rules = Lists.newArrayList(Splitter.on(".").trimResults().split(rule));
    for (int i = rules.size(); i < 5; i++) {
      rules.add("*");
    }
    List<String> ips = Lists.newArrayList(Splitter.on(".").trimResults().split(clientIp));
    for (int i = 0; i < 4; i++) {
      String r = rules.get(i);
      String ip = ips.get(i);
      if (!r.equals(ip) && !"*".equals(r)) {
        return false;
      }
    }
    return true;
  }
}
