package com.github.edgar615.direwolves.dispatch;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Edgar on 2016/10/14.
 *
 * @author Edgar  Date 2016/10/14
 */
public class ApiContextUtils {
  private ApiContextUtils() {
    throw new AssertionError("Not instantiable: " + ApiContextUtils.class);
  }

  private static Multimap<String, String> toMultimap(MultiMap multiMap) {
    Multimap<String, String> guavaMultimap = ArrayListMultimap.create();
    Set<String> names = multiMap.names();
    for (String paramName : names) {
      guavaMultimap.putAll(paramName, multiMap.getAll(paramName));
    }
    return guavaMultimap;
  }

  public static ApiContext apiContext(RoutingContext rc) {
    String path = rc.normalisedPath();
    HttpMethod method = rc.request().method();
    Multimap<String, String> headers = toMultimap(rc.request().headers());
    Multimap<String, String> params = toMultimap(rc.request().params());
    JsonObject body = null;
    if (method == HttpMethod.POST || method == HttpMethod.PUT
            || method == HttpMethod.DELETE) {
      if (rc.getBody() != null && rc.getBody().length() > 0) {
        try {
          body = rc.getBodyAsJson();
        } catch (DecodeException e) {
          throw SystemException.create(DefaultErrorCode.INVALID_JSON)
                  .set("details", "Request body must be JSON Object");
        }
      }
    }
    String id = (String) rc.data().getOrDefault("x-request-id", UUID.randomUUID().toString());
    long createdOn = System.currentTimeMillis();
    rc.data().put("apiCreatedOn", createdOn);
    ApiContext apiContext = ApiContext.create(id, method, path, headers, params, body);
    apiContext.addVariable("apiCreatedOn", createdOn);
    Map<String, Object> variables = getVariables(rc);
    variables.forEach((key, value) -> apiContext.addVariable(key, value));
    return apiContext;
  }

  private static Map<String, Object> getVariables(RoutingContext rc) {
    Map<String, Object> variables = new HashMap<>();
    HttpServerRequest req = rc.request();
    variables.put("request_time", System.currentTimeMillis());
//    variables.put("request.scheme", req.scheme());
//    variables.put("request.method", req.method().name());
//    variables.put("request.query_string", req.query());
//    variables.put("request.uri", req.uri());
    variables.put("request_path", req.path());
    variables.put("request_clientIp", getClientIp(req));
    return variables;
  }

  private static String getClientIp(HttpServerRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (!Strings.isNullOrEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
      //多次反向代理后会有多个ip值，第一个ip才是真实ip
      int index = ip.indexOf(",");
      if (index != -1) {
        return ip.substring(0, index);
      } else {
        return ip;
      }
    }
    ip = request.getHeader("X-Real-IP");
    if (!Strings.isNullOrEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
      return ip;
    }
    return request.remoteAddress().host();
  }

}
