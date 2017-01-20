package com.edgar.direwolves.core.definition;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by Edgar on 2017/1/3.
 *
 * @author Edgar  Date 2017/1/3
 */
@ProxyGen
public interface ApiProvider {

  /**
   * 根据请求方法和路径匹配对应的API
   *
   * @param method  请求方法
   * @param path    请求路径
   * @param handler 回调函数
   */
  void match(String method, String path, Handler<AsyncResult<JsonObject>> handler);

  /**
   * 根据名称返回API列表
   * @param name API名称，支持通配符
   * @param handler 回调函数
   */
  void list(String name, Handler<AsyncResult<List<JsonObject>>> handler);

  /**
   * 新增或者更新插件
   * @param name API名称，支持通配符
   * @param pluginJson 插件的JSON配置，可以包含多个JSON配置
   * @param handler 回调函数
   */
  void addPlugin(String name, JsonObject pluginJson, Handler<AsyncResult<JsonObject>> handler);
}
