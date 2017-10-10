package com.github.edgar615.direwolves.plugin.acl;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ACL限制的filter.
 * 该filter从API上下文中读取读取调用方的group（角色），<code>user.group</code>变量，
 * 如果这个角色属于白名单，直接允许访问（不在考虑黑名单）；如果这个角色属于黑名单，直接返回1004的错误.
 * 如果没有user.group变量，直接返回1004的错误.
 * <p>
 * *</pre>
 * *该filter可以接受下列的配置参数
 * <pre>
 * user.groupKey 用户组的键值，默认值group
 * </pre>
 * 该filter的order=1100
 * <p>
 * 接受的参数：
 * "acl.restriction" : {
 * "blacklist": ["guest],
 * "whitelist": ["user],
 * "groupKey": "role"
 * }
 * Created by edgar on 16-12-24.
 */
public class AclRestrictionFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AclRestrictionFilter.class);

  private final List<String> globalBlacklist = new ArrayList<>();

  private final List<String> globalWhitelist = new ArrayList<>();

  private final String groupKey;

  public AclRestrictionFilter(JsonObject config) {
    JsonObject jsonObject = config.getJsonObject("acl.restriction", new JsonObject());
    JsonArray blackArray = jsonObject.getJsonArray("blacklist", new JsonArray());
    JsonArray whiteArray = jsonObject.getJsonArray("whitelist", new JsonArray());
    for (int i = 0; i < blackArray.size(); i++) {
      globalBlacklist.add(blackArray.getString(i));
    }
    for (int i = 0; i < whiteArray.size(); i++) {
      globalWhitelist.add(whiteArray.getString(i));
    }
    this.groupKey = jsonObject.getString("groupKey", "group");
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 1100;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.principal() == null) {
      return true;
    }
    return !globalBlacklist.isEmpty()
           || !globalWhitelist.isEmpty()
           || apiContext.apiDefinition().plugin(AclRestrictionPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    AclRestrictionPlugin plugin = (AclRestrictionPlugin) apiContext.apiDefinition()
            .plugin(AclRestrictionPlugin.class.getSimpleName());
    List<String> blacklist = new ArrayList<>(globalBlacklist);
    List<String> whitelist = new ArrayList<>(globalWhitelist);
    if (plugin != null) {
      blacklist.addAll(plugin.blacklist());
      whitelist.addAll(plugin.whitelist());
    }
    if (apiContext.principal() == null) {
      completeFuture.complete(apiContext);
      return;
    }
    String group = apiContext.principal().getString(groupKey, "anonymous");
    List<String> black = blacklist.stream()
            .filter(r -> checkGroup(r, group))
            .collect(Collectors.toList());
    List<String> white = whitelist.stream()
            .filter(r -> checkGroup(r, group))
            .collect(Collectors.toList());
    if (white.isEmpty() && !black.isEmpty()) {
      Log.create(LOGGER)
              .setTraceId(apiContext.id())
              .setEvent("acl.restriction.tripped")
              .warn();
      completeFuture.fail(SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
                                  .set("details", "The group is forbidden"));
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