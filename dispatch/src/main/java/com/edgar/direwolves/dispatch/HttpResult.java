package com.edgar.direwolves.dispatch;

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
     * 创建一个JsonObject的响应.
     *
     * @param key          键值
     * @param statusCode   响应码
     * @param responseBody 响应
     * @return HttpResult
     */
    static HttpResult createJsonObject(String key, int statusCode,
                                       JsonObject responseBody) {
        return new HttpResultImpl(key, statusCode, responseBody);
    }

    /**
     * 创建一个JsonArray的响应.
     *
     * @param key           键值
     * @param statusCode    响应码
     * @param responseArray 响应
     * @return HttpResult
     */
    static HttpResult createJsonArray(String key, int statusCode,
                                      JsonArray responseArray) {
        return new HttpResultImpl(key, statusCode, responseArray);
    }

    /**
     * 将buffer转换为AsyncResult，如果转换为JSON数组失败，尝试转换为JSON对象，
     * 同理，如果转换为JSON对象失败，尝试转换为JSON数组.
     *
     * @param key        请求键值
     * @param statusCode 响应码
     * @param data       响应数据
     * @return HttpResult
     */
    static HttpResult create(String key, int statusCode, Buffer data) {
        String str = data.toString().trim();
        if (str.startsWith("{") && str.endsWith("}")) {
            try {
                return createJsonObject(key, statusCode,
                        Buffer.buffer(str).toJsonObject());
            } catch (Exception e) {
                throw SystemException.wrap(DefaultErrorCode.INVALID_JSON, e);
            }
        }
        if (str.startsWith("[") && str.endsWith("]")) {
            try {
                return createJsonArray(key, statusCode,
                        Buffer.buffer(str).toJsonArray());
            } catch (Exception e) {
                throw SystemException.wrap(DefaultErrorCode.INVALID_JSON, e);
            }
        }
        throw SystemException.create(DefaultErrorCode.INVALID_JSON);
    }

    /**
     * @return 响应名称，可以用来在一组结果中组合.
     */
    String name();

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
     * 请求的发起时间.
     *
     * @return
     */
    long startTime();

    /**
     * 请求的结束时间.
     *
     * @return
     */
    long endTime();

    /**
     * 耗时时间=endTime - startTime .
     *
     * @return
     */
    long elapsedTime();

    /**
     * 设置开始时间.
     *
     * @param startTime
     */
    void setStartTime(long startTime);

    /**
     * 设置endTime.
     *
     * @param endTime
     */
    void setEndTime(long endTime);


    /**
     * 返回result的id.
     * @return
     */
    String id();

    /**
     * 设置result的id.
     * @param id
     */
    void setId(String id);
}
