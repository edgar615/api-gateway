package com.edgar.direwolves.core.dispatch;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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

  static ApiContext create(HttpMethod method, String path, Multimap<String, String> headers,
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

  default Object getValueByKeyword(Object value) {
    if (value instanceof String) {
      String val = (String) value;
      //路径参数
      if (val.startsWith("$header.")) {
        List<String> list =
            Lists.newArrayList(headers().get(val.substring("$header.".length())));
        if (list.isEmpty()) {
          return null;
        } else if (list.size() == 1) {
          return list.get(0);
        } else {
          return list;
        }
      } else if (val.startsWith("$query.")) {
        List<String> list =
            Lists.newArrayList(params().get(val.substring("$query.".length())));
        if (list.isEmpty()) {
          return null;
        } else if (list.size() == 1) {
          return list.get(0);
        } else {
          return list;
        }
      } else if (val.startsWith("$body.")) {
        if (body() == null) {
          return null;
        }
        return body().getValue(val.substring("$body.".length()));
      } else if (val.startsWith("$user.")) {
        if (principal() == null) {
          return null;
        }
        return principal().getValue(val.substring("$user.".length()));
      } else if (val.startsWith("$var.")) {
        return variables().get(val.substring("$var.".length()));
      } else {
        return val;
      }
    } else if (value instanceof JsonArray) {
      JsonArray val = (JsonArray) value;
      JsonArray replacedArray = new JsonArray();
      for (int i = 0; i < val.size(); i++) {
        replacedArray.add(getValueByKeyword(val.getValue(i)));
      }
      return replacedArray;
    } else if (value instanceof JsonObject) {
      JsonObject val = (JsonObject) value;
      JsonObject replacedObject = new JsonObject();
      for (String key : val.fieldNames()) {
        replacedObject.put(key, getValueByKeyword(val.getValue(key)));
      }
      return replacedObject;
    } else {
      return value;
    }
  }
}
