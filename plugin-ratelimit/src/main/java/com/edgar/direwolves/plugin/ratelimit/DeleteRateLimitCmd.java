package com.edgar.direwolves.plugin.ratelimit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cmd.ApiSubCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.util.validation.Rule;
import io.vertx.core.json.JsonObject;

/**
 * 删除限流的命令.
 * 命令字:ratelimit.delete
 * 参数：type:类型，可选值second | minute | hour | day，非必填
 * 参数：key:限流键值，可选值user, appkey, ip，非必填
 *
 * @author Edgar  Date 2017/1/22
 */
public class DeleteRateLimitCmd implements ApiSubCmd {

  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  public DeleteRateLimitCmd() {
    rules.put("type", Rule.optional(Lists.newArrayList("second", "minute", "hour", "day")));

    rules.put("key", Rule.optional(Lists.newArrayList("user", "ip", "appkey")));
  }

  @Override
  public String cmd() {
    return "ratelimit.delete";
  }

  @Override
  public void handle(ApiDefinition definition, JsonObject jsonObject) {

//    Validations.validate(jsonObject.getMap(), rules);
//    String type = jsonObject.getString("type");
//    String key = jsonObject.getString("key");
//    RateLimiterPlugin plugin =
//            (RateLimiterPlugin) definition.plugin(RateLimiterPlugin.class.getSimpleName());
//    if (plugin != null) {
//      plugin.removeRateLimiter(key, type);
//    }

  }
}
