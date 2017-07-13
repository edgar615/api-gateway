package com.edgar.direwolves.core.apidiscovery;

import com.edgar.direwolves.core.definition.ApiDefinition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.function.Function;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
public interface ApiDiscovery {

  void close();

  static ApiDiscovery create(Vertx vertx, ApiDiscoveryOptions options) {
    return new ApiDiscoveryImpl(vertx, options);
  }

  /**
   * 向注册表中添加一个路由映射.
   * 映射表中name必须唯一.重复添加的数据会覆盖掉原来的映射.
   * @param definition 路由映射
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
   * @param name name
   * @param resultHandler 回调函数
   */
  void unpublish(String name, Handler<AsyncResult<Void>> resultHandler);

  void getDefinitions(JsonObject filter,
                      Handler<AsyncResult<List<ApiDefinition>>> resultHandler);

  void getDefinitions(Function<ApiDefinition, Boolean> filter,
                      Handler<AsyncResult<List<ApiDefinition>>> resultHandler);

  void getDefinition(String name,
                     Handler<AsyncResult<ApiDefinition>> resultHandler);
}
