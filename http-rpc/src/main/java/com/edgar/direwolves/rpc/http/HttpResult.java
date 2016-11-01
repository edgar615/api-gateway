package com.edgar.direwolves.rpc.http;

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
public interface HttpResult {

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
   * @return HttpResult
   */
  static HttpResult createJsonObject(String id, int statusCode,
                                     JsonObject responseBody, long elapsedTime) {
    return new HttpResultImpl(id, statusCode, responseBody, elapsedTime);
  }

  /**
   * 创建一个JsonArray的响应.
   *
   * @param id            id
   * @param statusCode    响应码
   * @param responseArray 响应
   * @return HttpResult
   */
  static HttpResult createJsonArray(String id, int statusCode,
                                    JsonArray responseArray, long elapsedTime) {
    return new HttpResultImpl(id, statusCode, responseArray, elapsedTime);
  }

  /**
   * 将buffer转换为AsyncResult，如果转换为JSON数组失败，尝试转换为JSON对象，
   * 同理，如果转换为JSON对象失败，尝试转换为JSON数组.
   *
   * @param id         id
   * @param statusCode 响应码
   * @param data       响应数据
   * @return HttpResult
   */
  static HttpResult create(String id, int statusCode, Buffer data, long elapsedTime) {
    String str = data.toString().trim();
    if (str.startsWith("{") && str.endsWith("}")) {
      try {
        return createJsonObject(id, statusCode,
                                Buffer.buffer(str).toJsonObject(), elapsedTime);
      } catch (Exception e) {
        throw SystemException.wrap(DefaultErrorCode.INVALID_JSON, e);
      }
    }
    if (str.startsWith("[") && str.endsWith("]")) {
      try {
        return createJsonArray(id, statusCode,
                               Buffer.buffer(str).toJsonArray(), elapsedTime);
      } catch (Exception e) {
        throw SystemException.wrap(DefaultErrorCode.INVALID_JSON, e);
      }
    }
    throw SystemException.create(DefaultErrorCode.INVALID_JSON);
  }

  default JsonObject toJson() {
    JsonObject result = new JsonObject()
            .put("id", id())
            .put("statusCode", statusCode())
            .put("isArray", isArray())
            .put("elapsedTime", elapsedTime());
    if (isArray()) {
      result.put("responseBody", responseObject());
    } else {
      result.put("responseArray", responseArray());
    }
    return result;
  }
}
