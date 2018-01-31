package com.github.edgar615.direwolves.core.dispatch;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.rpc.RpcRequest;
import com.github.edgar615.direwolves.core.rpc.RpcResponse;
import com.github.edgar615.direwolves.core.utils.NumberUtils;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
  ApiContext setPrincipal(JsonObject principal);

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
  ApiContext addVariable(String name, Object value);

  /**
   * @return api定义
   */
  ApiDefinition apiDefinition();

  /**
   * 设置api定义
   *
   * @param apiDefinition
   */
  ApiContext setApiDefinition(ApiDefinition apiDefinition);

  /**
   * @return 经过requestTransformer后的请求.
   */
  List<RpcRequest> requests();

  /**
   * @param request 将endpoint转换后的RPC请求
   */
  ApiContext addRequest(RpcRequest request);

  /**
   * @return RPC请求的结果.
   */
  List<RpcResponse> responses();

  /**
   * @param response RPC请求的结果
   */
  ApiContext addResponse(RpcResponse response);

  /**
   * @return 最终聚合的需要返回给调用方的响应
   */
  Result result();

  /**
   * @param result 设置最终的结果
   */
  ApiContext setResult(Result result);

  /**
   * 计算RPC的ID，主要用于日志记录
   * @return ID
   */
  String nextRpcId();

  /**
   * 创建ApiContext对象.
   *
   * @param id      上下文ID，该值应该使用一个全局唯一ID
   * @param method  HTTP方法
   * @param path    请求路径
   * @param headers 请求头
   * @param params  请求参数
   * @param body    请求体
   * @return ApiContext对象
   */
  static ApiContext create(String id, HttpMethod method, String path,
                           Multimap<String, String> headers,
                           Multimap<String, String> params, JsonObject body) {
    return new ApiContextImpl(id, method, path, headers, params, body);
  }

  /**
   * 创建ApiContext对象.该方法使用一个UUID作为ID.
   *
   * @param method  HTTP方法
   * @param path    请求路径
   * @param headers 请求头
   * @param params  请求参数
   * @param body    请求体
   * @return ApiContext对象
   */
  static ApiContext create(HttpMethod method, String path, Multimap<String, String> headers,
                           Multimap<String, String> params, JsonObject body) {
    return create(UUID.randomUUID().toString(), method, path, headers, params, body);
  }

  /**
   * 将上下文中的可变属性复制到另外一个上下文中.
   *
   * <b>最初对于ApiDefinition属性同样使用了copy()
   * 方法复制一个新对象，但是在基准测试时发现当plugin数量变多时，copy的性能会随之下降。考虑到ApiDefinition
   * 实际上是一个不可变对象（创建后不会修改），这里不再使用ApiDefinition.copy()方法，而是直接将ApiDefinition赋值给新的ApiContext</b>
   * @param source 源对象
   * @param target 目标对象
   */
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
//      target.setApiDefinition(source.apiDefinition().copy());
      target.setApiDefinition(source.apiDefinition());
    }
  }

  /**
   * 将ApiContext复制成为一个新的对象
   *
   * @return ApiContext
   */
  default ApiContext copy() {
    ApiContext apiContext;
    if (body() == null) {
      apiContext = new ApiContextImpl(id(), method(), path(), ArrayListMultimap.create(headers()),
                                      ArrayListMultimap.create(params()), null);
    } else {
      JsonObject copyJson = copyJson(body());
      apiContext = new ApiContextImpl(id(), method(), path(), ArrayListMultimap.create(headers()),
                                      ArrayListMultimap.create(params()), copyJson);
    }

    ApiContext.copyProperites(this, apiContext);
    return apiContext;
  }

  default JsonObject copyJson(JsonObject jsonObject) {//直接使用原生的JsonObject.copy方法，会导致JSON的KEY顺序错乱，校验签名的算法会失效
    JsonObject copyJson = new JsonObject();
    for (String key : jsonObject.fieldNames()) {
      copyJson.put(key, jsonObject.getValue(key));
    }
    return copyJson;
  }

  /**
   * 根据约定的格式从上下文中读取值.
   * <p>
   * $header.xxx从请求头中读取，如果该参数只有一个值[val]，直接返回这个值val，如果该参数有多个值，返回这些值的列表[val1, val2]
   * $query.xxx从请求参数中读取，如果该参数只有一个值[val]，直接返回这个值val，如果该参数有多个值，返回这些值的列表[val1, val2]
   * $body.xxx从请求体中读取，如果body为null，直接返回null
   * $user.xxx从请求的用户数据中读取，如果用户为null，直接返回null
   * $var.xxx从请求的变量中读取
   * <p>
   * 其他未定义的格式直接返回传入的参数，如传入$test.foo，直接返回$test.fo
   *
   * @param key 参数名
   * @return
   */
  default Object getValueByKeyword(String key) {
    if (key.startsWith("$header.")) {
      List<String> list =
              Lists.newArrayList(headers().get(key.substring("$header." .length())));
      if (list.isEmpty()) {
        return null;
      } else if (list.size() == 1) {
        return list.get(0);
      } else {
        return list;
      }
    } else if (key.startsWith("$query.")) {
      List<String> list =
              Lists.newArrayList(params().get(key.substring("$query." .length())));
      if (list.isEmpty()) {
        return null;
      } else if (list.size() == 1) {
        //query需要判断一下字符串是不是数字，是数字的直接转为对应的数字
        String value = list.get(0);
        Number number = NumberUtils.tryParse(value);
        if (number == null) {
          return value;
        }
        return number;
      } else {
        return list;
      }
    } else if (key.startsWith("$body.")) {
      if (body() == null) {
        return null;
      }
      return body().getValue(key.substring("$body." .length()));
    } else if (key.startsWith("$user.")) {
      if (principal() == null) {
        return null;
      }
      return principal().getValue(key.substring("$user." .length()));
    } else if (key.startsWith("$var.")) {
      return variables().get(key.substring("$var." .length()));
    } else {
      return key;
    }
  }

}
