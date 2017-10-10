package com.github.edgar615.direwolves.core.rpc;

import com.google.common.base.MoreObjects;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 单个请求的响应.
 * <p>
 * <b>responseObject和responseArray只有一个有效</b>
 * responseArray不为null时，responseObject必为null,isArray必为true.
 *
 * @author Edgar  Date 2016/4/8
 */
class RpcResponseImpl implements RpcResponse {

  /**
   * 请求的唯一id，用来区分多个请求的结果.
   */
  private final String id;

  /**
   * 响应码
   */
  private final int statusCode;

  /**
   * 响应是否是数组.
   * <p>
   * responseObject不为null时,isArray必为false.
   * responseArray不为null时，isArray必为true.
   */
  private final boolean isArray;

  /**
   * JsonObject格式的响应.
   * <p>
   * <b>responseObject和responseArray只有一个有效</b>
   * responseObject不为null时，responseArray必为null,isArray必为false
   */
  private final JsonObject responseObject;

  /**
   * JsonArray格式的响应.
   * <b>responseObject和responseArray只有一个有效</b>
   * responseArray不为null时，responseObject必为null,isArray必为true.
   */
  private final JsonArray responseArray;

  /**
   * 耗时.
   */
  private final long elapsedTime;

  RpcResponseImpl(String id, int statusCode, JsonObject responseObject, long elapsedTime) {
    this.id = id;
    this.statusCode = statusCode;
    this.responseObject = responseObject;
    this.isArray = false;
    this.responseArray = null;
    this.elapsedTime = elapsedTime;
  }

  RpcResponseImpl(String id, int statusCode, JsonArray responseArray, long elapsedTime) {
    this.id = id;
    this.statusCode = statusCode;
    this.responseArray = responseArray;
    this.isArray = true;
    this.responseObject = null;
    this.elapsedTime = elapsedTime;
  }

  @Override
  public int statusCode() {
    return statusCode;
  }

  @Override
  public boolean isArray() {
    return isArray;
  }

  /**
   * 返回JsonObject格式的响应内容.
   *
   * @return JsonObject
   */
  @Override
  public JsonObject responseObject() {
    return responseObject;
  }

  /**
   * 返回JsonArray格式的响应内容.
   *
   * @return JsonArray
   */
  @Override
  public JsonArray responseArray() {
    return responseArray;
  }


  @Override
  public long elapsedTime() {
    return elapsedTime;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String toString() {
    MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("RpcResponse");
    helper.add("id", id)
            .add("statusCode", statusCode);
    if (isArray) {
      helper.add("responseArray", responseArray);
    } else {
      helper.add("responseObject", responseObject);
    }
    helper.add("elapsedTime", elapsedTime);
    return helper.toString();
  }
}
