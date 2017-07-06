package com.edgar.servicediscovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.List;

/**
 * Created by Edgar on 2017/6/8.
 *
 * @author Edgar  Date 2017/6/8
 */
public interface MoreServiceDiscovery {

  static MoreServiceDiscovery create(Vertx vertx) {
    return new MoreServiceDiscoveryImpl(vertx);
  }

  static MoreServiceDiscovery create(Vertx vertx, MoreServiceDiscoveryOptions options) {
    return new MoreServiceDiscoveryImpl(vertx, options);
  }

  ServiceDiscovery discovery();

  /**
   * 根据名称查询所有的服务节点
   *
   * @param name    服务名称
   * @param handler 回调函数
   */
  void queryAllInstances(String name, Handler<AsyncResult<List<Record>>> handler);

  /**
   * 查询所有的服务节点
   *
   * @param handler 回调函数
   */
  void queryAllInstances(Handler<AsyncResult<List<Record>>> handler);

  /**
   * 根据名称，查询一个服务节点
   *
   * @param name    服务节点
   * @param handler 回调函数
   */
  void queryForInstance(String name, Handler<AsyncResult<Record>> handler);

  /**
   * 根据名称和ID，查询一个服务节点
   *
   * @param name    服务名称
   * @param id 节点ID
   * @param handler 回调函数
   */
  void queryForInstance(String name, String id, Handler<AsyncResult<Record>> handler);

  /**
   * 统计所有的服务.
   * @param handler 回调函数，返回的json对象：{"服务名":{"instances":节点数量},"..}
   */
  void queryForNames(Handler<AsyncResult<JsonObject>> handler);
}
