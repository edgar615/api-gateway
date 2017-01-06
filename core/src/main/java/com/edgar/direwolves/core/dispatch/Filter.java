package com.edgar.direwolves.core.dispatch;

import com.google.common.collect.ImmutableList;

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
public interface Filter {

  List<FilterFactory> factories = ImmutableList.copyOf(ServiceLoader.load(FilterFactory.class));

  String PRE = "PRE";

  String POST = "POST";

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
   * 该方法的第二个参数用于将filter传递给下一个filter,
   * completeFuture.complete(apiContext)会执行下一个Filter，completeFuture.fail()会抛出异常，跳出filter的执行.
   *
   * @param apiContext     api上下文
   * @param completeFuture completeFuture.complete()传递给下一个filter,completeFuture.fail(),异常
   */
  void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture);

  /**
   * 创建一个过滤器.
   *
   * @param name   过滤器的名称
   * @param vertx  Vertx
   * @param config 配置
   * @return Filter
   */
  static Filter create(String name, Vertx vertx, JsonObject config) {
    List<FilterFactory> list = factories.stream()
            .filter(f -> name.equalsIgnoreCase(f.name()))
            .collect(Collectors.toList());
    if (list.isEmpty()) {
      throw new NoSuchElementException("no such factory->" + name);
    }
    return list.get(0).create(vertx, config);
  }

}
