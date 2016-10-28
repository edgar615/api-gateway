package com.edgar.direwolves.core.dispatch;

import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * API调用的上下文.
 *
 * @author Edgar  Date 2016/10/10
 */
public interface ApiContext {

  static ApiContext apiContext(HttpMethod method, String path, Multimap<String, String> headers,
                                      Multimap<String, String> params, JsonObject body) {
    return new ApiContextImpl(method, path, headers, params, body);
  }


  /**
   * @return ID，全局唯一.
   */
  String id();

  /**
   * @return url参数
   */
  Multimap<String, String> params();

  /**
   * @return 请求头
   */
  Multimap<String, String> headers();

  /**
   * @return 请求体, json格式.
   */
  JsonObject body();

  /**
   * @return 请求路径.
   */
  String path();

  /**
   * @return 请求方法
   */
  HttpMethod method();

  /**
   * @return 用户信息.
   */
  JsonObject principal();

  /**
   * 设置用户信息.
   *
   * @param principal 用户信息
   */
  void setPrincipal(JsonObject principal);

  /**
   * @return 变量
   */
  Map<String, Object> variables();

  /**
   * 增加变量
   *
   * @param name  变量名
   * @param value 变量值
   */
  void addVariable(String name, Object value);

  /**
   * @return 服务地址
   */
  List<JsonObject> services();

  /**
   * 增加record
   *
   * @param record
   */
  void addService(JsonObject record);

  /**
   * @return api定义
   */
  ApiDefinition apiDefinition();

  /**
   * 设置api定义
   *
   * @param apiDefinition
   */
  void setApiDefinition(ApiDefinition apiDefinition);


  /**
   * @return 经过requestTransformer后的请求.
   */
  JsonArray request();

  /**
   * @param jsonObject 添加一个经过requestTransformer后的请求
   */
  void addRequest(JsonObject jsonObject);

  /**
   * @return 经过requestTransformer后的请求.
   */
  JsonArray response();

  /**
   * @param jsonObject 添加一个经过responseTransformer后的请求
   */
  void addResponse(JsonObject jsonObject);

  /**
   * @return ApiContext
   */
  ApiContext copy();
}
