package com.edgar.direwolves.core.dispatch;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.vertx.core.http.HttpMethod;
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

  static void copyProperites(ApiContext source, ApiContext target) {

    target.setPrincipal(source.principal());

    source.variables().forEach((key, value) -> target.addVariable(key, value));
    for (int i = 0; i < source.requests().size(); i++) {
      target.addRequest(source.requests().get(i).copy());
    }
    for (int i = 0; i < source.responses().size(); i++) {
      target.addResponse(source.responses().get(i).copy());
    }
    if (source.result() != null) {
      target.setResult(source.result().copy());
    }
    if (source.apiDefinition() != null) {
      target.setApiDefinition(source.apiDefinition().copy());
    }
    for (Map.Entry<String, ApiContext> action : source.actions()) {
      target.addAction(action.getKey(), action.getValue());
    }
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
  List<RpcRequest> requests();

  /**
   * @param request 将endpoint转换后的RPC请求
   */
  void addRequest(RpcRequest request);

  /**
   * @return RPC请求的结果.
   */
  List<RpcResponse> responses();

  /**
   * @param response RPC请求的结果
   */
  void addResponse(RpcResponse response);

  /**
   * @return 最终聚合的需要返回给调用方的响应
   */
  Result result();

  /**
   * @param result 设置最终的结果
   */
  void setResult(Result result);

  /**
   * 添加一个动作，主要记录每次动作对ApiContext的改变，用来做日志跟踪.
   *
   * @param action     动作名称
   * @param apiContext
   */
  void addAction(String action, ApiContext apiContext);

  /**
   * @return 返回所有的动作.
   */
  List<Map.Entry<String, ApiContext>> actions();

  /**
   * @return ApiContext
   */
  default ApiContext copy() {
    ApiContext apiContext;
    if (body() == null) {
      apiContext = new ApiContextImpl(method(), path(), ArrayListMultimap.create(headers()),
          ArrayListMultimap.create(params()), null);
    } else {
      apiContext = new ApiContextImpl(method(), path(), ArrayListMultimap.create(headers()),
          ArrayListMultimap.create(params()), body().copy());
    }

    ApiContext.copyProperites(this, apiContext);
    return apiContext;
  }

  //  default Object getValueByKeyword(Object value) {
//    if (value instanceof String) {
//      String val = (String) value;
//      //路径参数
//      if (val.startsWith("$header.")) {
//        List<String> list =
//            Lists.newArrayList(headers().get(val.substring("$header.".length())));
//        if (list.isEmpty()) {
//          return null;
//        } else if (list.size() == 1) {
//          return list.get(0);
//        } else {
//          return list;
//        }
//      } else if (val.startsWith("$query.")) {
//        List<String> list =
//            Lists.newArrayList(params().get(val.substring("$query.".length())));
//        if (list.isEmpty()) {
//          return null;
//        } else if (list.size() == 1) {
//          return list.get(0);
//        } else {
//          return list;
//        }
//      } else if (val.startsWith("$body.")) {
//        if (body() == null) {
//          return null;
//        }
//        return body().getValue(val.substring("$body.".length()));
//      } else if (val.startsWith("$user.")) {
//        if (principal() == null) {
//          return null;
//        }
//        return principal().getValue(val.substring("$user.".length()));
//      } else if (val.startsWith("$var.")) {
//        return variables().get(val.substring("$var.".length()));
//      } else {
//        return val;
//      }
//    } else if (value instanceof JsonArray) {
//      JsonArray val = (JsonArray) value;
//      JsonArray replacedArray = new JsonArray();
//      for (int i = 0; i < val.size(); i++) {
//        Object newVal = getValueByKeyword(val.getValue(i));
//        if (newVal != null) {
//          replacedArray.add(newVal);
//        }
//      }
//      return replacedArray.isEmpty() ? null : replacedArray;
//    } else if (value instanceof JsonObject) {
//      JsonObject val = (JsonObject) value;
//      JsonObject replacedObject = new JsonObject();
//      for (String key : val.fieldNames()) {
//        Object newVal = getValueByKeyword(val.getValue(key));
//        if (newVal != null) {
//          replacedObject.put(key, newVal);
//        }
//      }
//      return replacedObject.isEmpty() ? null : replacedObject;
//    } else {
//      return value;
//    }
//  }
  default Object getValueByKeyword(String val) {
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
  }
}
