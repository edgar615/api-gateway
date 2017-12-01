package com.github.edgar615.direwolves.plugin.user;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User限制的filter.
 * 该filter从API上下文中读取读取调用方的userId，
 * 白名单包含允许访问的userId，来自白名单的userId始终运行访问。但不在白名单中的userId不会禁止访问
 * 黑名单包含不允许访问的userId，来自黑名单的userId始终禁止访问
 * <p>
 * *该filter可以接受下列的配置参数
 * 该filter的order=10500
 * <p>
 * 接受的参数：
 * "user.restriction" : {
 * "blacklist": [1],
 * "whitelist": [2]
 * }
 * Created by edgar on 16-12-24.
 */
public class UserRestrictionFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserRestrictionFilter.class);

  private final List<String> globalBlacklist = new ArrayList<>();

  private final List<String> globalWhitelist = new ArrayList<>();

  private final String userKey = "userId";

  public UserRestrictionFilter(JsonObject config) {
    JsonObject jsonObject = config.getJsonObject("user.restriction", new JsonObject());
    JsonArray blackArray = jsonObject.getJsonArray("blacklist", new JsonArray());
    JsonArray whiteArray = jsonObject.getJsonArray("whitelist", new JsonArray());
    for (int i = 0; i < blackArray.size(); i++) {
      globalBlacklist.add(blackArray.getValue(i).toString());
    }
    for (int i = 0; i < whiteArray.size(); i++) {
      globalWhitelist.add(whiteArray.getValue(i).toString());
    }
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 10500;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.principal() == null) {
      return false;
    }
    return !globalBlacklist.isEmpty()
           || !globalWhitelist.isEmpty()
           || apiContext.apiDefinition().plugin(UserRestrictionPlugin.class.getSimpleName())
              != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    UserRestrictionPlugin plugin = (UserRestrictionPlugin) apiContext.apiDefinition()
            .plugin(UserRestrictionPlugin.class.getSimpleName());
    List<String> blacklist = new ArrayList<>(globalBlacklist);
    List<String> whitelist = new ArrayList<>(globalWhitelist);
    if (plugin != null) {
      blacklist.addAll(plugin.blacklist());
      whitelist.addAll(plugin.whitelist());
    }
    String userId = apiContext.principal().getValue(userKey).toString();

    //匹配到白名单则允许通过
    if (satisfyList(userId, whitelist)) {
      completeFuture.complete(apiContext);
      return;
    }
    //匹配到黑名单则禁止通过
    if (satisfyList(userId, blacklist)) {
      SystemException e = SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
              .set("details", "The user is forbidden");
      failed(completeFuture, apiContext.id(), "user.tripped", e);
      return;
    }
    completeFuture.complete(apiContext);

  }

  private boolean satisfyList(String userId, List<String> list) {
    return list.stream()
                   .filter(r -> checkUser(r, userId))
                   .count() > 0;
  }

  private boolean checkUser(String rule, String group) {
    if ("*".equals(rule)) {
      return true;
    }
    return rule.equalsIgnoreCase(group);
  }

}