package com.edgar.direwolves.filter;

import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Splitter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edgar on 16-9-20.
 */
public class ScopeFilter implements Filter {

    private static final String TYPE = "scope";

    public ScopeFilter() {
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void config(Vertx vertx, JsonObject config) {
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
        if (!apiContext.getVariables().containsKey("scope")) {
            completeFuture.fail(SystemException.create(DefaultErrorCode.NO_AUTHORITY));
        }
        String scope = (String) apiContext.getVariables().get("scope");
        List<String> scopeList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(scope);
        scopeList = new ArrayList<>(scopeList);
        scopeList.add("default");
        String apiScope = apiContext.getApiDefinition().scope();
        if (!scopeList.contains("all") && !scopeList.contains(apiScope)) {
            completeFuture.fail(SystemException.create(DefaultErrorCode.NO_AUTHORITY));
        } else {
            completeFuture.complete(apiContext);
        }
    }

}
