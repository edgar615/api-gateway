//package com.edgar.direwolves.filter;
//
//import com.google.common.base.Splitter;
//
//import com.edgar.direwolves.definition.ApiDefinition;
//import com.edgar.direwolves.dispatch.ApiContext;
//import com.edgar.direwolves.dispatch.filter.Filter;
//import com.edgar.util.exception.DefaultErrorCode;
//import com.edgar.util.exception.SystemException;
//import io.vertx.core.Future;
//import io.vertx.core.Vertx;
//import io.vertx.core.json.JsonObject;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 校验调用方是否有该app的访问权限
// * Created by edgar on 16-9-20.
// */
//public class ScopeFilter implements Filter {
//
//  private static final String NAME = "scope";
//
//  @Override
//  public String name() {
//    return NAME;
//  }
//
//  @Override
//  public String type() {
//    return PRE;
//  }
//
//  @Override
//  public void config(Vertx vertx, JsonObject config) {
//  }
//
//  @Override
//  public boolean shouldFilter(ApiContext apiContext) {
//    ApiDefinition apiDefinition = apiContext.apiDefinition();
//    if (apiDefinition == null) {
//      return false;
//    }
//    List<String> filters = apiDefinition.filters();
//    return filters.contains(NAME)
//           && !"default".equalsIgnoreCase(apiDefinition.scope());
//  }
//
//  @Override
//  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
//    if (!apiContext.variables().containsKey("scope")) {
//      completeFuture.fail(SystemException.create(DefaultErrorCode.NO_AUTHORITY));
//      return;
//    }
//    String scope = (String) apiContext.variables().get("scope");
//    List<String> scopeList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(scope);
//    scopeList = new ArrayList<>(scopeList);
//    scopeList.add("default");
//    String apiScope = apiContext.apiDefinition().scope();
//    if (!scopeList.contains("all") && !scopeList.contains(apiScope)) {
//      completeFuture.fail(SystemException.create(DefaultErrorCode.NO_AUTHORITY));
//    } else {
//      completeFuture.complete(apiContext);
//    }
//  }
//
//}
