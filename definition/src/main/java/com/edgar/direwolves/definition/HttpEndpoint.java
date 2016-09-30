package com.edgar.direwolves.definition;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Created by Edgar on 2016/9/14.
 * 按照remove --> replace --> add的顺序执行
 *
 * @author Edgar  Date 2016/9/14
 */
public interface HttpEndpoint extends Endpoint {
    String TYPE = "http-endpoint";

  List<Map<String, String>> bodyArgsReplace();

  List<Map<String, String>> bodyArgsAdd();

  List<String> bodyArgsRemove();

  List<Map<String, String>> urlArgsReplace();

  List<Map<String, String>> urlArgsAdd();

  List<String> urlArgsRemove();

  List<Map<String, String>> headersReplace();

  List<Map<String, String>> headersAdd();

  List<String> headersRemove();

  static HttpEndpoint fromJson(JsonObject jsonObject) {
        return HttpEndpointDecoder.instance().apply(jsonObject);
    }

    default String type() {
        return TYPE;
    }

    /**
     * @return 名称，必填项，全局唯一.
     */
    String name();

    /**
     * @return 请求方法 GET | POST | DELETE | PUT.
     */
    HttpMethod method();

    /**
     * API路径
     * 示例：/tasks，匹配请求：/tasks.
     * 示例：/tasks/$param1，匹配请求：/tasks/变量param1.
     *
     * @return API路径
     */
    String path();

    /**
     * @return 服务名，用于服务发现.
     */
    String service();

    /**
     * @return URL参数
     */
    List<Parameter> urlArgs();

    /**
     * @return body参数
     */
    List<Parameter> bodyArgs();

    /**
     * 增加一个header
     *
     * @param key
     * @param value
     * @return
     */
    HttpEndpoint addRequestHeader(String key, String value);

    /**
     * 替换一个header,只有当header存在时才替换;
     *
     * @param key
     * @param value
     * @return
     */
    HttpEndpoint replaceRequestHeader(String key, String value);

    /**
     * 删除一个header
     *
     * @param key
     * @return
     */
    HttpEndpoint removeHeader(String key);

    /**
     * 增加一个url_arg
     *
     * @param key
     * @param value
     * @return
     */
    HttpEndpoint addRequestUrlArg(String key, String value);

    /**
     * 替换一个url_arg,只有当url_arg存在时才替换;
     *
     * @param key
     * @param value
     * @return
     */
    HttpEndpoint replaceRequestUrlArg(String key, String value);

    /**
     * 删除一个url_arg
     *
     * @param key
     * @return
     */
    HttpEndpoint removeUrlArg(String key);

    /**
     * 增加一个body_arg
     *
     * @param key
     * @param value
     * @return
     */
    HttpEndpoint addRequestBodyArg(String key, String value);

    /**
     * 替换一个body_arg,只有当body_arg存在时才替换;
     *
     * @param key
     * @param value
     * @return
     */
    HttpEndpoint replaceRequestBodyArg(String key, String value);

    /**
     * 删除一个body_arg
     *
     * @param key
     * @return
     */
    HttpEndpoint removeBodyArg(String key);
}
