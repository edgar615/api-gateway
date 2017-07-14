package com.edgar.direwolves.core.apidiscovery;

import com.edgar.direwolves.core.definition.ApiDefinition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.function.Function;

/**
 * API的发现模块.
 * <p>
 * 整个功能实现参考了ServiceDiscovery模块
 *
 * @author Edgar  Date 2017/6/20
 */
public interface ApiDiscovery extends ApiPublisher {

  /**
   * 注册一个ApiImporter.
   * @param importer API的导入实现
   * @param config JSON配置
   * @param completionHandler importer启动之后的回调函数
   * @return ApiDiscovery
   */
  ApiDiscovery registerServiceImporter(ApiImporter importer, JsonObject config,
                                       Handler<AsyncResult<Void>> completionHandler);

  void close();

  void getDefinitions(JsonObject filter,
                      Handler<AsyncResult<List<ApiDefinition>>> resultHandler);

  void getDefinitions(Function<ApiDefinition, Boolean> filter,
                      Handler<AsyncResult<List<ApiDefinition>>> resultHandler);

  void getDefinition(String name,
                     Handler<AsyncResult<ApiDefinition>> resultHandler);

  static ApiDiscovery create(Vertx vertx, ApiDiscoveryOptions options) {
    return new ApiDiscoveryImpl(vertx, options);
  }
}
