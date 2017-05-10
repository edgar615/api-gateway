package com.edgar.direwolves.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 17-5-10.
 */
public interface ServiceDiscoveryInternal extends ServiceDiscovery {

  /**
   * 发布一个服务节点.
   *
   * @param instance 服务节点
   */
  void publish(ServiceInstance instance);

  /**
   * 删除一个服务节点.
   *
   * @param id  服务节点id
   */
  void unpublish(String id);

  ServiceDiscovery registerServiceImporter(ServiceImporter importer, JsonObject configuration,
                                           Handler<AsyncResult<Void>> completionHandler);
}
