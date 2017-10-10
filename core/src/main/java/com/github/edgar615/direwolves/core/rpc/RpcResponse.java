package com.github.edgar615.direwolves.core.rpc;

import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
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
        throw SystemException.wrap(DefaultErrorCode.INVALID_JSON, e)
                .set("details", "The result of RPC is incorrect, It should be a JSON object");
      }
    }
    if (data.startsWith("[") && data.endsWith("]")) {
      try {
        return createJsonArray(id, statusCode,
                               Buffer.buffer(data).toJsonArray(), elapsedTime);
      } catch (Exception e) {
        throw SystemException.wrap(DefaultErrorCode.INVALID_JSON, e)
                .set("details", "The result of RPC is incorrect, It should be a JSON object");
      }
    }
    throw SystemException.create(DefaultErrorCode.INVALID_JSON)
            .set("details", "The result of RPC is incorrect, It should be a JSON object");
  }

  default RpcResponse copy() {
    if (isArray()) {
      return RpcResponse.createJsonArray(id(), statusCode(), responseArray().copy(), elapsedTime());
    }
    return RpcResponse.createJsonObject(id(), statusCode(), responseObject().copy(), elapsedTime());
  }

}
