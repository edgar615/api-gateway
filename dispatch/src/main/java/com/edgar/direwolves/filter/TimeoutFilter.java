package com.edgar.direwolves.filter;

import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.util.base.EncryptUtils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by edgar on 16-9-20.
 */
public class TimeoutFilter implements Filter {

    private Vertx vertx;
    private static final String TYPE = "timeout";
    private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();
    private int timeout = 5 * 60;
    private JsonArray secrets = new JsonArray();

    public TimeoutFilter() {
//        commonParamRule.put("nonce", Rule.required());
        commonParamRule.put("timestamp", Rule.required());
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void config(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        if (config.containsKey("timeout.expires")) {
            this.timeout = config.getInteger("timeout.expires");
        }
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
      if (apiContext.getApiDefinition() == null) {
        return false;
      }
      List<String> filters = apiContext.getApiDefinition().filters();
      return filters.contains(TYPE);
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
        //校验参数
        Validations.validate(apiContext.params(), commonParamRule);
        Multimap<String, String> params = ArrayListMultimap.create(apiContext.params());
        //检查时间戳
        Integer timestamp = Integer.parseInt(getFirst(params, "timestamp").toString());
        long currentTime = Instant.now().getEpochSecond();
        if ((timestamp > currentTime + timeout)
                || (timestamp < currentTime - timeout)) {
            completeFuture.fail(SystemException.create(DefaultErrorCode.EXPIRE));
        } else {
            completeFuture.complete(apiContext);
        }
    }

    private String getFirst(Multimap<String, String> params, String paramName) {
        return Lists.newArrayList(params.get(paramName)).get(0);
    }

}
