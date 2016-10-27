package com.edgar.direwolves.dispatch;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.spi.ApiContext;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Edgar on 2016/10/14.
 *
 * @author Edgar  Date 2016/10/14
 */
public class Utils {

  public static ApiContext apiContext(HttpMethod method, String path, Multimap<String, String> headers,
                           Multimap<String, String> params, JsonObject body) {
    return new ApiContextImpl(method, path, headers, params, body);
  }

  public static ApiContext apiContext(RoutingContext rc) {
    String path = rc.normalisedPath();
    HttpMethod method = rc.request().method();
    Multimap<String, String> headers =
            MultiMapToMultimap.instance().apply(rc.request().headers());
    Multimap<String, String> params =
            MultiMapToMultimap.instance().apply(rc.request().params());
    JsonObject body = null;
    if (rc.getBody().length() > 0) {
      try {
        body = rc.getBodyAsJson();
      } catch (DecodeException e) {
        throw SystemException.create(DefaultErrorCode.INVALID_JSON);
      }
    }
    ApiContext apiContext =  new ApiContextImpl(method, path, headers, params, body);
    Map<String, String> variables = getVariables(rc);
    variables.forEach((key, value) -> apiContext.addVariable(key, value));
    return apiContext;
  }

  private static Map<String, String> getVariables(RoutingContext rc) {
    Map<String, String> variables = new HashMap<>();
    HttpServerRequest req = rc.request();
    variables.put("request.scheme", req.scheme());
    variables.put("request.method", req.method().name());
    variables.put("request.query_string", req.query());
    variables.put("request.uri", req.uri());
    variables.put("request.path_info", req.path());
    variables.put("request.client_ip", req.remoteAddress().host());
    return variables;
  }

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
        jsonObject.put(key, Lists.newArrayList(values));
//        if (values.size() > 1) {
//          jsonObject.put(key, Lists.newArrayList(values));
//        } else {
//          jsonObject.put(key, Iterables.get(values, 0));
//        }
      }
    });
    return jsonObject;
  }
}