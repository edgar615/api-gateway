package com.github.edgar615.gateway.core.apidiscovery;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
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
 * <p>
 * <b>注意</b>
 * 如果backend依赖于下游资源（DB、SyncMap），ApiDiscovery有些时候的性能可能不够理想，所以最终将discovery直接依赖于本地缓存.
 *
 * @author Edgar  Date 2017/6/20
 */
public interface ApiDiscovery extends ApiPublisher {

    /**
     * 注册一个ApiImporter.
     *
     * @param importer          API的导入实现
     * @param config            JSON配置
     * @param completionHandler importer启动之后的回调函数
     * @return ApiDiscovery
     */
    ApiDiscovery registerImporter(ApiImporter importer, JsonObject config,
                                  Handler<AsyncResult<Void>> completionHandler);

    void close();

    /**
     * 根据请求方法、路径快速匹配API
     *
     * @param method
     * @param path
     * @param resultHandler
     */
    void filter(String method, String path,
                Handler<AsyncResult<List<ApiDefinition>>> resultHandler);

    /**
     * API的搜索
     *
     * @param filter
     * @param resultHandler
     */
    void getDefinitions(JsonObject filter,
                        Handler<AsyncResult<List<ApiDefinition>>> resultHandler);

    /**
     * API的搜索
     *
     * @param filter
     * @param resultHandler
     */
    void getDefinitions(Function<ApiDefinition, Boolean> filter,
                        Handler<AsyncResult<List<ApiDefinition>>> resultHandler);

    /**
     * 根据名称查找API
     *
     * @param name
     * @param resultHandler
     */
    void getDefinition(String name,
                       Handler<AsyncResult<ApiDefinition>> resultHandler);

    ApiDiscoveryOptions options();

    /**
     * 清除所有api
     *
     * @param completionHandler
     */
    void clear(Handler<AsyncResult<Void>> completionHandler);

    static ApiDiscovery create(Vertx vertx, ApiDiscoveryOptions options) {
        return new ApiDiscoveryImpl(vertx, options);
    }
}
