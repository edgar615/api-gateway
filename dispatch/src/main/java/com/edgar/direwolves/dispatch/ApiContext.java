package com.edgar.direwolves.dispatch;

import com.google.common.collect.Multimap;

import com.edgar.direwolves.definition.ApiDefinition;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

/**
 * API调用的上下文.
 *
 * @author Edgar  Date 2016/10/10
 */
public interface ApiContext {

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

  static ApiContext create(HttpMethod method, String path, Multimap<String, String> headers,
                           Multimap<String, String> params, JsonObject body) {
    return new ApiContextImpl(method, path, headers, params, body);
  }

  static ApiContext create(RoutingContext rc) {
    String path = rc.normalisedPath();
    HttpMethod method = rc.request().method();
    Multimap<String, String> headers =
            MultiMapToMultimap.instance().apply(rc.request().headers());
    Multimap<String, String> params =
            MultiMapToMultimap.instance().apply(rc.request().params());
    JsonObject body = null;
    if (rc.getBody().length() > 0) {
      try {
        body = rc.getBodyAsJson();
      } catch (DecodeException e) {
        throw SystemException.create(DefaultErrorCode.INVALID_JSON);
      }
    }
    ApiContext apiContext = create(method, path, headers, params, body);
    Map<String, String> variables = getVariables(rc);
    variables.forEach((key, value) -> apiContext.addVariable(key, value));
    return apiContext;
  }

  static Map<String, String> getVariables(RoutingContext rc) {
    Map<String, String> variables = new HashMap<>();
    HttpServerRequest req = rc.request();
    variables.put("request.scheme", req.scheme());
    variables.put("request.method", req.method().name());
    variables.put("request.query_string", req.query());
    variables.put("request.uri", req.uri());
    variables.put("request.path_info", req.path());
    variables.put("request.client_ip", req.remoteAddress().host());
    return variables;
  }
}
