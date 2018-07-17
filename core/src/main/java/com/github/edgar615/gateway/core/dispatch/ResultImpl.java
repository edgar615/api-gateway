package com.github.edgar615.gateway.core.dispatch;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

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
class ResultImpl implements Result {

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

  private final Multimap<String, String> headers = ArrayListMultimap.create();

  private final int byteSize;

  ResultImpl(int statusCode, JsonObject responseObject,
             Multimap<String, String> headers) {
    this.statusCode = statusCode;
    this.responseObject = responseObject;
    this.isArray = false;
    this.responseArray = null;
    if (headers != null) {
      this.headers.putAll(headers);
    }
    byteSize = responseObject.encode().getBytes().length;
  }

  ResultImpl(int statusCode, JsonArray responseArray, Multimap<String, String> headers) {
    this.statusCode = statusCode;
    this.responseArray = responseArray;
    this.isArray = true;
    this.responseObject = null;
    if (headers != null) {
      this.headers.putAll(headers);
    }
    byteSize = responseArray.encode().getBytes().length;
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
    return responseObject == null ? null : responseObject;
  }

  /**
   * 返回JsonArray格式的响应内容.
   *
   * @return JsonArray
   */
  @Override
  public JsonArray responseArray() {
    return responseArray == null ? null : responseArray;
  }

  @Override
  public Multimap<String, String> headers() {
    return ImmutableMultimap.copyOf(headers);
  }

  @Override
  public int byteSize() {
    return byteSize;
  }

  @Override
  public String toString() {
    MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("Result");
    helper.add("statusCode", statusCode);
    if (isArray) {
      helper.add("responseArray", responseArray);
    } else {
      helper.add("responseObject", responseObject);
    }
    helper.add("headers", headers);
    return helper.toString();
  }

  @Override
  public Result addHeader(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  @Override
  public Result addHeaders(Multimap<String, String> header) {
    this.headers.putAll(header);
    return this;
  }

}
