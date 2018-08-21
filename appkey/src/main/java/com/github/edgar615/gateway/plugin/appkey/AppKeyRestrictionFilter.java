package com.github.edgar615.gateway.plugin.appkey;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * AppKey限制的filter.
 * 该filter从API上下文中读取读取调用方的appKey，<code>app
 * .appKey</code>变量
 * <p>
 * 白名单包含允许访问的appKey，来自白名单的appKey始终运行访问。但不在白名单中的appKey不会禁止访问
 * 黑名单包含不允许访问的appKey，来自黑名单的appKey始终禁止访问
 * 如果没有appKey变量，直接允许.
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
        if (!apiContext.variables().containsKey("client_appKey")) {
            return false;
        }
        return !globalBlacklist.isEmpty()
               || !globalWhitelist.isEmpty()
               || apiContext.apiDefinition().plugin(AppKeyRestriction.class.getSimpleName())
                  != null;
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
        String appKey = (String) apiContext.variables().getOrDefault("client_appKey", "anonymous");
        //匹配到白名单则允许通过
        if (satisfyList(appKey, whitelist)) {
            completeFuture.complete(apiContext);
            return;
        }
        //匹配到黑名单则禁止通过
        if (satisfyList(appKey, blacklist)) {
            SystemException e = SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
                    .set("details", "The appKey is forbidden");
            failed(completeFuture, apiContext.id(), "appKey.tripped", e);
            return;
        }
        completeFuture.complete(apiContext);
    }

    private boolean satisfyList(String appKey, List<String> list) {
        return list.stream()
                       .filter(r -> checkAppKey(r, appKey))
                       .count() > 0;
    }

    private boolean checkAppKey(String rule, String appKey) {
        if ("*".equals(rule)) {
            return true;
        }
        return rule.equalsIgnoreCase(appKey);
    }
}
