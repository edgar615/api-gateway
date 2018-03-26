package com.github.edgar615.direwolves.plugin.acl;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Log;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ACL限制的filter.
 * 该filter从principal中读取读取调用方的group,这个group的类型是字符串，
 * <p>
 * 白名单包含允许访问的group，来自白名单的group始终运行访问。但不在白名单中的group不会禁止访问
 * 黑名单包含不允许访问的group，来自黑名单的group始终禁止访问
 * 如果没有group变量，按照匿名用户anonymous处理.
 * <p>
 * *</pre>
 * *该filter可以接受下列的配置参数
 * 该filter的order=12000
 * <p>
 * 接受的参数：
 * "acl.restriction" : {
 * "blacklist": ["guest],
 * "whitelist": ["user]
 * }
 * Created by edgar on 16-12-24.
 */
public class AclRestrictionFilter implements Filter {

  private final List<String> globalBlacklist = new ArrayList<>();

  private final List<String> globalWhitelist = new ArrayList<>();

  private final String groupKey = "group";

  public AclRestrictionFilter(JsonObject config) {
    updateConfig(config);
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 12000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.principal() == null) {
      return false;
    }
    return !globalBlacklist.isEmpty()
           || !globalWhitelist.isEmpty()
           || apiContext.apiDefinition().plugin(AclRestriction.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    AclRestriction plugin = (AclRestriction) apiContext.apiDefinition()
            .plugin(AclRestriction.class.getSimpleName());
    List<String> blacklist = new ArrayList<>(globalBlacklist);
    List<String> whitelist = new ArrayList<>(globalWhitelist);
    if (plugin != null) {
      blacklist.addAll(plugin.blacklist());
      whitelist.addAll(plugin.whitelist());
    }
    String group = apiContext.principal().getString(groupKey, "anonymous");
    //匹配到白名单则允许通过
    if (satisfyList(group, whitelist)) {
      completeFuture.complete(apiContext);
      return;
    }
    //匹配到黑名单则禁止通过
    if (satisfyList(group, blacklist)) {
      SystemException systemException = SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
              .set("details", "The group is forbidden");
      failed(completeFuture, apiContext.id(), "AclForbidden", systemException);
      return;
    }
    completeFuture.complete(apiContext);
  }

  @Override
  public void updateConfig(JsonObject config) {
    if (config.getValue("acl.restriction") instanceof JsonObject) {
      JsonObject jsonObject = config.getJsonObject("acl.restriction", new JsonObject());
      JsonArray blackArray = jsonObject.getJsonArray("blacklist", new JsonArray());
      JsonArray whiteArray = jsonObject.getJsonArray("whitelist", new JsonArray());
      globalBlacklist.clear();
      globalWhitelist.clear();
      for (int i = 0; i < blackArray.size(); i++) {
        globalBlacklist.add(blackArray.getString(i));
      }
      for (int i = 0; i < whiteArray.size(); i++) {
        globalWhitelist.add(whiteArray.getString(i));
      }
    }
  }

  private boolean satisfyList(String group, List<String> list) {
    return list.stream()
                   .filter(r -> checkGroup(r, group))
                   .count() > 0;
  }

  private boolean checkGroup(String rule, String group) {
    if ("*".equals(rule)) {
      return true;
    }
    return rule.equalsIgnoreCase(group);
  }

}