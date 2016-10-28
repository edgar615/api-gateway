package com.edgar.direwolves.core.dispatch;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.spi.Configurable;
import com.edgar.direwolves.core.dispatch.ApiContext;
import io.vertx.core.Future;

import java.util.List;

/**
 * api请求的过滤器.
 * Created by edgar on 16-9-18.
 */
public interface Filter extends Configurable {

  String PRE = "pre";

  String POST = "post";

  /**
   * @return filter的名称
   */
  String name();

  /**
   * @return filter的类型 pre或者post
   */
  String type();

  /**
   * 根据上下文判断是否应该执行filter的方法
   *
   * @param apiContext api上下文
   * @return true 执行filter，false 忽略
   */
  boolean shouldFilter(ApiContext apiContext);

  /**
   * filter的处理方法.
   * 该方法的第二个参数用于将filter传递给下一个filter.
   *
   * @param apiContext     api上下文
   * @param completeFuture completeFuture.complete()传递给下一个filter,completeFuture.fail(),异常
   */
  void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture);

  /**
   * 获取Multimap中的第一个参数.
   *
   * @param params
   * @param paramName
   * @return
   */
  default String getFirst(Multimap<String, String> params, String paramName) {
    List<String> values = Lists.newArrayList(params.get(paramName));
    if (values.isEmpty()) {
      return null;
    }
    return values.get(0);
  }
}
