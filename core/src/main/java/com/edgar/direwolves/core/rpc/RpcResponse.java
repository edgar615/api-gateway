package com.edgar.direwolves.core.rpc;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/9/21.
 *
 * @author Edgar  Date 2016/9/21
 */
public interface RpcResponse {

  /**
   * @return 响应码.
   */
  int statusCode();

  /**
   * @return true:返回的结果是json数组, false:返回的结果是json对象.
   */
  boolean isArray();

  /**
   * @return json对象
   */
  JsonObject responseObject();

  /**
   * @return json数组
   */
  JsonArray responseArray();

  /**
   * 耗时时间=endTime - startTime .
   *
   * @return
   */
  long elapsedTime();

  /**
   * 返回result的id.
   *
   * @return
   */
  String id();

  /**
   * 创建一个JsonObject的响应.
   *
   * @param id           id
   * @param statusCode   响应码
   * @param responseBody 响应
   * @return RpcResponse
   */
  static RpcResponse createJsonObject(String id, int statusCode,
                                      JsonObject responseBody, long elapsedTime) {
    return new RpcResponseImpl(id, statusCode, responseBody, elapsedTime);
  }

  /**
   * 创建一个JsonArray的响应.
   *
   * @param id            id
   * @param statusCode    响应码
   * @param responseArray 响应
   * @return RpcResponse
   */
  static RpcResponse createJsonArray(String id, int statusCode,
                                     JsonArray responseArray, long elapsedTime) {
    return new RpcResponseImpl(id, statusCode, responseArray, elapsedTime);
  }

  /**
   * 将buffer转换为AsyncResult，如果转换为JSON数组失败，尝试转换为JSON对象，
   * 同理，如果转换为JSON对象失败，尝试转换为JSON数组.
   *
   * @param id          id
   * @param statusCode  响应码
   * @param data        响应数据
   * @param elapsedTime 耗时
   * @return RpcResponse
   */
  static RpcResponse create(String id, int statusCode, Buffer data, long elapsedTime) {
    String str = data.toString().trim();
    return create(id, statusCode, str, elapsedTime);
  }

  /**
   * 将buffer转换为AsyncResult，如果转换为JSON数组失败，尝试转换为JSON对象，
   * 同理，如果转换为JSON对象失败，尝试转换为JSON数组.
   *
   * @param id          id
   * @param statusCode  响应码
   * @param data        响应数据
   * @param elapsedTime 耗时
   * @return RpcResponse
   */
  static RpcResponse create(String id, int statusCode, String data, long elapsedTime) {
    if (data.startsWith("{") && data.endsWith("}")) {
      try {
        return createJsonObject(id, statusCode,
                                Buffer.buffer(data).toJsonObject(), elapsedTime);
      } catch (Exception e) {
        throw SystemException.wrap(DefaultErrorCode.INVALID_JSON, e);
      }
    }
    if (data.startsWith("[") && data.endsWith("]")) {
      try {
        return createJsonArray(id, statusCode,
                               Buffer.buffer(data).toJsonArray(), elapsedTime);
      } catch (Exception e) {
        throw SystemException.wrap(DefaultErrorCode.INVALID_JSON, e);
      }
    }
    throw SystemException.create(DefaultErrorCode.INVALID_JSON);
  }

  default RpcResponse copy() {
    if (isArray()) {
      return RpcResponse.createJsonArray(id(), statusCode(), responseArray().copy(), elapsedTime());
    }
    return RpcResponse.createJsonObject(id(), statusCode(), responseObject().copy(), elapsedTime());
  }

  default JsonObject toJson() {
    JsonObject result = new JsonObject()
            .put("id", id())
            .put("statusCode", statusCode())
            .put("isArray", isArray())
            .put("elapsedTime", elapsedTime());
    if (isArray()) {
      result.put("responseArray", responseArray());
    } else {
      result.put("responseBody", responseObject());
    }
    return result;
  }
}
