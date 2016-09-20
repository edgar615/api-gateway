package com.edgar.direwolves.filter;

import com.edgar.direwolves.dispatch.ApiContext;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-9-18.
 */
public interface Filter {

    /**
     * @return filter的名称
     */
    String type();

    /**
     * 在类创建之后执行初始化操作方法.
     * 我们可以在这个类里读取配置，初始化变量.
     *
     * @param vertx  Vertx
     * @param config 配置属性
     */
    void config(Vertx vertx, JsonObject config);

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
}
