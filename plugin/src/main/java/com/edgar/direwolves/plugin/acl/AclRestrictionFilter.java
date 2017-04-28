package com.edgar.direwolves.plugin.acl;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ACL限制的filter.
 * 该filter从API上下文中读取读取调用方的group（角色），<code>user.group</code>变量，
 * 如果这个角色属于白名单，直接允许访问（不在考虑黑名单）；如果这个角色属于黑名单，直接返回1004的错误.
 * 如果没有user.group变量，直接返回1004的错误.
 * <p>
 * <p>
 * 该filter的order=1100
 * Created by edgar on 16-12-24.
 */
public class AclRestrictionFilter implements Filter {

  AclRestrictionFilter() {
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
    return apiContext.apiDefinition().plugin(AclRestrictionPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    AclRestrictionPlugin plugin = (AclRestrictionPlugin) apiContext.apiDefinition()
            .plugin(AclRestrictionPlugin.class.getSimpleName());
    boolean allow;
    if (apiContext.principal() != null && apiContext.principal().containsKey("group")) {
      String group = apiContext.principal().getString("group");
      List<String> black = plugin.blacklist().stream()
              .filter(r -> checkGroup(r, group))
              .collect(Collectors.toList());
      List<String> white = plugin.whitelist().stream()
              .filter(r -> r.equalsIgnoreCase(group))
              .collect(Collectors.toList());
      if (white.isEmpty() && !black.isEmpty()) {
        allow = false;
      } else {
        allow = true;
      }
    } else {
      allow = false;
    }
    if (allow) {
      completeFuture.complete(apiContext);
    } else {
      completeFuture.fail(SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
                                  .set("details", "The group is forbidden"));
    }

  }

  private boolean checkGroup(String rule, String group) {
    if (!rule.equals(group) && !"*".equals(rule)) {
      return false;
    }
    return true;
  }

}