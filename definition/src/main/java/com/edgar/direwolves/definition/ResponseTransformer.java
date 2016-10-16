package com.edgar.direwolves.definition;

import java.util.List;
import java.util.Map;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public interface ResponseTransformer {

    /**
     * @return body的替换规则.
     */
    List<Map.Entry<String, String>> respBodyReplace();

    /**
     * @return body的增加规则
     */
    List<Map.Entry<String, String>> respBodyAdd();

    /**
     * @return body的删除规则
     */
    List<String> respBodyRemove();

    /**
     * @return header的替换规则
     */
    List<Map.Entry<String, String>> respHeadersReplace();

    /**
     * @return header的新增规则
     */
    List<Map.Entry<String, String>> respHeadersAdd();

    /**
     * @return header的删除规则
     */
    List<String> respHeadersRemove();

    /**
     * 增加一个header
     *
     * @param key
     * @param value
     * @return
     */
    HttpEndpoint addRespHeader(String key, String value);

    /**
     * 替换一个header,只有当header存在时才替换;
     *
     * @param key
     * @param value
     * @return
     */
    HttpEndpoint replaceRespHeader(String key, String value);

    /**
     * 删除一个header
     *
     * @param key
     * @return
     */
    HttpEndpoint removeRespHeader(String key);

    /**
     * 增加一个body
     *
     * @param key
     * @param value
     * @return
     */
    HttpEndpoint addRespBody(String key, String value);

    /**
     * 替换一个body,只有当body存在时才替换;
     *
     * @param key
     * @param value
     * @return
     */
    HttpEndpoint replaceRespBody(String key, String value);

    /**
     * 删除一个body
     *
     * @param key
     * @return
     */
    HttpEndpoint removeRespBody(String key);
}
