package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.utils.MultiMapToMultimap;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Edgar on 2016/10/14.
 *
 * @author Edgar  Date 2016/10/14
 */
public class Utils {

  public static ApiContext apiContext(RoutingContext rc) {
    String path = rc.normalisedPath();
    HttpMethod method = rc.request().method();
    Multimap<String, String> headers =
        MultiMapToMultimap.instance().apply(rc.request().headers());
    Multimap<String, String> params =
        MultiMapToMultimap.instance().apply(rc.request().params());
    JsonObject body = null;
    if (rc.getBody() != null && rc.getBody().length() > 0) {
      try {
        body = rc.getBodyAsJson();
      } catch (DecodeException e) {
        throw SystemException.create(DefaultErrorCode.INVALID_JSON);
      }
    }
    ApiContext apiContext = ApiContext.create(method, path, headers, params, body);
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
    variables.put("request.client_ip", getClientIp(req));
    return variables;
  }

  public static String getClientIp(HttpServerRequest request) {
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
