package com.edgar.direwolves.dispatch;

import com.google.common.base.MoreObjects;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 单个请求的响应.
 * <p/>
 * <b>responseObject和responseArray只有一个有效</b>
 * responseArray不为null时，responseObject必为null,isArray必为true.
 *
 * @author Edgar  Date 2016/4/8
 */
class HttpResultImpl implements HttpResult {

    /**
     * 请求的键值，用来区分多个请求的结果.
     */
    private final String name;
    /**
     * 响应码
     */
    private final int statusCode;
    /**
     * 响应是否是数组.
     * <p/>
     * responseObject不为null时,isArray必为false.
     * responseArray不为null时，isArray必为true.
     */
    private final boolean isArray;
    /**
     * JsonObject格式的响应.
     * <p/>
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
     * 请求的唯一id.
     */
    private String id;
    private long startTime = 0;

    private long endTime = 0;

    HttpResultImpl(String name, int statusCode, JsonObject responseObject) {
        this.name = name;
        this.statusCode = statusCode;
        this.responseObject = responseObject;
        this.isArray = false;
        this.responseArray = null;
    }

    HttpResultImpl(String name, int statusCode, JsonArray responseArray) {
        this.name = name;
        this.statusCode = statusCode;
        this.responseArray = responseArray;
        this.isArray = true;
        this.responseObject = null;
    }

    @Override
    public String name() {
        return name;
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
    public long startTime() {
        return startTime;
    }

    @Override
    public long endTime() {
        return endTime;
    }

    @Override
    public long elapsedTime() {
        return endTime - startTime;
    }

    /**
     * 设置开始时间.
     *
     * @param startTime
     */
    @Override
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * 设置endTime.
     *
     * @param endTime
     */
    @Override
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("HttpResult");
        helper.add("id", id)
                .add("name", name)
                .add("statusCode", statusCode);
        if (isArray) {
            helper.add("responseArray", responseArray);
        } else {
            helper.add("responseObject", responseObject);
        }
        helper.add("startTime", startTime)
                .add("endTime", endTime)
                .add("elapsedTime", elapsedTime());
        return helper.toString();
    }
}
