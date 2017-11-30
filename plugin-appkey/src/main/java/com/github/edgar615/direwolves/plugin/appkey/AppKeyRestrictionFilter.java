package com.github.edgar615.direwolves.plugin.appkey;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AppKey限制的filter.
 * 该filter从API上下文中读取读取调用方的appKey，<code>app
 * .appKey</code>变量，如果这个appKey属于白名单，直接允许访问（不在考虑黑名单）；如果这个appKey属于黑名单，直接返回1004的错误.
 * <p>
 * <p>
 * 该filter的order=11
 * <p>
 * 该filter可以接受下列的配置参数
 * <pre>
 *     "appKey.restriction" : {
 * "whitelist" : [],
 * "blacklist" : []
 * }
 * </pre>
 * Created by edgar on 16-12-24.
 */
public class AppKeyRestrictionFilter implements Filter {

  private final List<String> globalBlacklist = new ArrayList<>();

  private final List<String> globalWhitelist = new ArrayList<>();


  public AppKeyRestrictionFilter(JsonObject config) {
    JsonObject jsonObject = config.getJsonObject("appKey.restriction", new JsonObject());
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
    return 8100;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (!apiContext.variables().containsKey("client.appKey")) {
      return false;
    }
    return !globalBlacklist.isEmpty()
           || !globalWhitelist.isEmpty()
           || apiContext.apiDefinition().plugin(AppKeyRestriction.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    AppKeyRestriction plugin = (AppKeyRestriction) apiContext.apiDefinition()
            .plugin(AppKeyRestriction.class.getSimpleName());
    List<String> blacklist = new ArrayList<>(globalBlacklist);
    List<String> whitelist = new ArrayList<>(globalWhitelist);
    if (plugin != null) {
      blacklist.addAll(plugin.blacklist());
      whitelist.addAll(plugin.whitelist());
    }
    String appKey = (String) apiContext.variables().getOrDefault("client.appKey", "anonymous");
    List<String> black = blacklist.stream()
            .filter(r -> checkGroup(r, appKey))
            .collect(Collectors.toList());
    List<String> white = whitelist.stream()
            .filter(r -> checkGroup(r, appKey))
            .collect(Collectors.toList());
    if (white.isEmpty() && !black.isEmpty()) {
      SystemException e = SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
              .set("details", "The appKey is forbidden");
      failed(completeFuture, apiContext.id(), "appKey.tripped", e);

    } else {
      completeFuture.complete(apiContext);
    }

  }

  private boolean checkGroup(String rule, String group) {
    if ("*".equals(rule)) {
      return true;
    }
    return rule.equalsIgnoreCase(group);
  }
}
