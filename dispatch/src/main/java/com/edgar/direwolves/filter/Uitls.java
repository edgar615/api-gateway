package com.edgar.direwolves.filter;

import com.edgar.direwolves.dispatch.ApiContext;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Set;

/**
 * Created by Edgar on 2016/10/14.
 *
 * @author Edgar  Date 2016/10/14
 */
public class Uitls {
    public static String replaceUrl(String url, ApiContext context) {
        //路径参数
        Set<String> paramNames = context.params().asMap().keySet();
        for (String name : paramNames) {
            url = url.replaceAll("\\$param." + name,
                    getFirst(context.params(), name));
        }
        Set<String> headerNames = context.headers().asMap().keySet();
        for (String name : headerNames) {
            url = url.replaceAll("\\$header." + name,
                    getFirst(context.params(), name));
        }
        if (context.body() != null) {
            Set<String> bodyNames = context.body().getMap().keySet();
            for (String name : bodyNames) {
                url = url.replaceAll("\\$body." + name,
                        context.body().getMap().get(name).toString());
            }
        }
        if (context.principal() != null) {

            Set<String> userNames = context.principal().getMap().keySet();
            for (String name : userNames) {
                url = url.replaceAll("\\$user." + name,
                        context.principal().getMap().get(name).toString());
            }

        }
        return url;
    }

    /**
     * 返回request transformer中出现的值。总是返回
     *
     * @param name     参数名
     * @param eldValue 旧值
     * @param context  API上下文
     * @return
     */
    public static JsonObject transformer(String name, String eldValue, ApiContext context) {
        JsonObject jsonObject = new JsonObject();
        //路径参数
        if (name.startsWith("$header.")) {
            List<String> list =
                    Lists.newArrayList(context.headers().get(name.substring("$header.".length())));
            if (list.size() == 1) {
                jsonObject.put(name, list.get(0));
            } else {
                jsonObject.put(name, list);
            }
        } else if (name.startsWith("$params.")) {
            List<String> list = Lists.newArrayList(context.params().get(name.substring("$param.".length()
            )));
            if (list.size() == 1) {
                jsonObject.put(name, list.get(0));
            } else {
                jsonObject.put(name, list);
            }
        } else if (name.startsWith("$body.")) {
            jsonObject.put(name, context.body().getValue(name.substring("$body.".length())));
        } else if (name.startsWith("$user.")) {
            jsonObject.put(name, context.principal().getValue(name.substring("$user.".length())));
        } else if (name.startsWith("$var.")) {
            jsonObject.put(name, context.variables().get(name.substring("$var.".length())));
        } else {
            jsonObject.put(name, eldValue);
        }
        return jsonObject;
    }

    /**
     * 获取Multimap中的第一个参数.
     *
     * @param params
     * @param paramName
     * @return
     */
    public static String getFirst(Multimap<String, String> params, String paramName) {
        List<String> values = Lists.newArrayList(params.get(paramName));
        if (values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    /**
     * 将Multimap转换为JsonObject参数.
     * 如果value集合中只有一个值，使用jsonobject.put(key, value.get(0))添加
     * 如果value集合中有多个值,使用jsonobject.put(key,value)添加
     *
     * @param map Multimap
     * @return JsonObject
     */
    public static JsonObject mutliMapToJson(Multimap<String, String> map) {
        JsonObject jsonObject = new JsonObject();
        map.asMap().forEach((key, values) -> {
            if (values != null) {
                if (values.size() > 1) {
                    jsonObject.put(key, Lists.newArrayList(values));
                } else {
                    jsonObject.put(key, Iterables.get(values, 0));
                }
            }
        });
        return jsonObject;
    }
}
