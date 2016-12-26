package com.edgar.direwolves.core.dispatch;

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

  private final Multimap<String, String> headers = ArrayListMultimap.create();

  ResultImpl(String id, int statusCode, JsonObject responseObject,
             Multimap<String, String> headers) {
    this.id = id;
    this.statusCode = statusCode;
    this.responseObject = responseObject;
    this.isArray = false;
    this.responseArray = null;
    if (headers != null) {
      this.headers.putAll(headers);
    }
  }

  ResultImpl(String id, int statusCode, JsonArray responseArray, Multimap<String, String> headers) {
    this.id = id;
    this.statusCode = statusCode;
    this.responseArray = responseArray;
    this.isArray = true;
    this.responseObject = null;
    if (headers != null) {
      this.headers.putAll(headers);
    }
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
  public String id() {
    return id;
  }

  @Override
  public Multimap<String, String> header() {
    return ImmutableMultimap.copyOf(headers);
  }

  @Override
  public String toString() {
    MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("Result");
    helper.add("id", id)
            .add("statusCode", statusCode);
    if (isArray) {
      helper.add("responseArray", responseArray);
    } else {
      helper.add("responseObject", responseObject);
    }
    helper.add("headers", headers);
    return helper.toString();
  }
}
