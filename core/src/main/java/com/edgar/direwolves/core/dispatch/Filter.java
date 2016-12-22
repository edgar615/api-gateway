package com.edgar.direwolves.core.dispatch;

import com.edgar.direwolves.core.spi.Configurable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * api请求的过滤器.
 * Created by edgar on 16-9-18.
 */
public interface Filter extends Configurable {

  List<FilterFactory> factories = ImmutableList.copyOf(ServiceLoader.load(FilterFactory.class));
  String PRE = "PRE";
  String POST = "POST";

  static Filter create(String name,Vertx vertx, JsonObject config) {
    List<FilterFactory> list = factories.stream()
        .filter(f -> name.equalsIgnoreCase(f.name()))
        .collect(Collectors.toList());
    if (list.isEmpty()) {
      throw new NoSuchElementException("no such factory->" + name);
    }
    return list.get(0).create(vertx, config);
  }

  default void config(Vertx vertx, JsonObject config) {

  }

  /**
   * @return filter的类型 pre或者post
   */
  String type();

  /**
   * @return filter的顺序
   */
  int order();

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
