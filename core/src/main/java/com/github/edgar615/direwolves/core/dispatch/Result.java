package com.github.edgar615.direwolves.core.dispatch;

import com.google.common.collect.Multimap;

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
public interface Result {

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
   * @return json对象
   */
  Multimap<String, String> headers();

  int byteSize();

  Result addHeader(String name, String value);

  Result addHeaders(Multimap<String, String> header);

  /**
   * 创建一个JsonObject的响应.
   *
   * @param statusCode   响应码
   * @param responseBody 响应
   * @param header       响应头
   * @return Result
   */
  static Result createJsonObject(int statusCode,
                                 JsonObject responseBody, Multimap<String, String> header) {
    return new ResultImpl(statusCode, responseBody, header);
  }

  /**
   * 创建一个JsonArray的响应.
   *
   * @param statusCode    响应码
   * @param responseArray 响应
   * @param header        响应头
   * @return Result
   */
  static Result createJsonArray(int statusCode,
                                JsonArray responseArray, Multimap<String, String> header) {
    return new ResultImpl(statusCode, responseArray, header);
  }

  /**
   * 将buffer转换为AsyncResult，如果转换为JSON数组失败，尝试转换为JSON对象，
   * 同理，如果转换为JSON对象失败，尝试转换为JSON数组.
   *
   * @param statusCode 响应码
   * @param data       响应数据
   * @param header     响应头
   * @return Result
   */
  static Result create(int statusCode, Buffer data, Multimap<String, String> header) {
    String str = data.toString().trim();
    return create(statusCode, str, header);
  }

  /**
   * 将buffer转换为AsyncResult，如果转换为JSON数组失败，尝试转换为JSON对象，
   * 同理，如果转换为JSON对象失败，尝试转换为JSON数组.
   *
   * @param statusCode 响应码
   * @param data       响应数据
   * @param header     响应头
   * @return Result
   */
  static Result create(int statusCode, String data, Multimap<String, String> header) {
    if (data.startsWith("{") && data.endsWith("}")) {
      try {
        return createJsonObject(statusCode,
                                Buffer.buffer(data).toJsonObject(), header);
      } catch (Exception e) {
        throw SystemException.wrap(DefaultErrorCode.INVALID_JSON, e)
                .set("details", "The result of RPC is incorrect, It should be a JSON object");
      }
    }
    if (data.startsWith("[") && data.endsWith("]")) {
      try {
        return createJsonArray(statusCode,
                               Buffer.buffer(data).toJsonArray(), header);
      } catch (Exception e) {
        throw SystemException.wrap(DefaultErrorCode.INVALID_JSON, e)
                .set("details", "The result of RPC is incorrect, It should be JSON array");
      }
    }
    throw SystemException.create(DefaultErrorCode.INVALID_JSON)
            .set("details", "The result of RPC is incorrect, It should be a JSON object");
  }

  default Result copy() {
    if (isArray()) {
      return Result.createJsonArray(statusCode(), responseArray().copy(), headers());
    }
    return Result.createJsonObject(statusCode(), responseObject(), headers());
  }

}
