package com.edgar.direwolves.plugin.ip;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IP限制的filter.
 * 该filter从API上下文中读取读取调用方的IP，<code>request
 * .client_ip</code>变量，如果这个IP属于白名单，直接允许访问（不在考虑黑名单）；如果这个IP属于黑名单，直接返回1004的错误.
 * <p>
 *
 *   该filter的order=100
 * Created by edgar on 16-12-24.
 */
public class IpRestrictionFilter implements Filter {

  private final  List<String> globalBlacklist = new ArrayList<>();
  private final  List<String> globalWhitelist = new ArrayList<>();


  public IpRestrictionFilter(JsonObject config) {
    JsonArray blackArray = config.getJsonArray("ip.blacklist", new JsonArray());
    JsonArray whiteArray = config.getJsonArray("ip.whitelist", new JsonArray());
    for (int i = 0; i < blackArray.size(); i ++) {
      globalBlacklist.add(blackArray.getString(i));
    }
    for (int i = 0; i < whiteArray.size(); i ++) {
      globalWhitelist.add(whiteArray.getString(i));
    }
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().plugin(IpRestriction.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    IpRestriction plugin =
            (IpRestriction) apiContext.apiDefinition().plugin(IpRestriction.class.getSimpleName());
    String clientIp = (String) apiContext.variables().get("request.client_ip");
    List<String> blacklist = new ArrayList<>(plugin.blacklist());
    blacklist.addAll(globalBlacklist);
    List<String> black = blacklist.stream()
            .filter(r -> checkIp(r, clientIp))
            .collect(Collectors.toList());
    List<String> whitelist = new ArrayList<>(plugin.whitelist());
    whitelist.addAll(globalWhitelist);
    List<String> white = whitelist.stream()
            .filter(r -> checkIp(r, clientIp))
            .collect(Collectors.toList());
    if (white.isEmpty() && !black.isEmpty()) {
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
