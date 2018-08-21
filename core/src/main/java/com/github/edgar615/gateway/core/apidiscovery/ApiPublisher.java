package com.github.edgar615.gateway.core.apidiscovery;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * API的发布与注销接口.
 *
 * @author Edgar  Date 2017/7/14
 */
public interface ApiPublisher {
    /**
     * 向注册表中添加一个路由映射.
     * 映射表中name必须唯一.重复添加的数据会覆盖掉原来的映射.
     *
     * @param definition    路由映射
     * @param resultHandler 回调函数
     */
    void publish(ApiDefinition definition, Handler<AsyncResult<ApiDefinition>> resultHandler);

    /**
     * 根据name删除符合的路由映射.
     * 如果name=null，会删除所有的路由映射.
     * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
     * *user会查询所有以user结尾对name,如add_user.
     * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
     *
     * @param name          name
     * @param resultHandler 回调函数
     */
    void unpublish(String name, Handler<AsyncResult<Void>> resultHandler);
}
