package com.github.edgar615.gateway.dispatch;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import com.github.edgar615.gateway.core.metric.ApiMetric;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * http请求的辅助类.所有的请求都可以接受这个处理类，该类主要做一下通用的设置然后就会将请求传递给下一个处理类.
 * <p>
 * 设置响应的content-type为application/json;charset=utf-8
 *
 * @author Edgar  Date 2016/2/18
 */
public class BaseHandler implements Handler<RoutingContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseHandler.class);

  public static Handler<RoutingContext> create() {
    return new BaseHandler();
  }

  @Override
  public void handle(RoutingContext rc) {
    rc.response().setChunked(true)
            .putHeader("content-type", "application/json;charset=utf-8");

    String id = UUID.randomUUID().toString();
    rc.put("x-request-id", id);
    long start = System.currentTimeMillis();
    rc.put("x-request-time", start);
    LOGGER.info("[{}] [SR] [HTTP] [{} {}] [{}] [{}] [{}] [{}]", id, rc.request().method().name(),
                rc.normalisedPath(),
                mutiMapToString(rc.request().headers(), "no header"),
                mutiMapToString(rc.request().params(), "no param"),
                body(rc), getClientIp(rc.request())
    );

    rc.addHeadersEndHandler(v -> {
      rc.response().putHeader("x-server-time",
                              ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    });

    rc.addBodyEndHandler(v -> {
      long duration = System.currentTimeMillis() - start;
      LOGGER.info("[{}] [SS] [HTTP] [{}] [{}] [{}bytes] [{}ms]", id,
                  rc.response().getStatusCode(),
                  mutiMapToString(rc.response().headers(), "no header"),
                  rc.response().bytesWritten(),
                  duration);
      responseMetric(rc, duration);
    });
    rc.next();
  }

  private String body(RoutingContext rc) {
    return (rc.getBody() == null || rc.getBody().length() == 0)
            ? "no body" : rc.getBody().toString();
  }

  private String getClientIp(HttpServerRequest request) {
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

  private void responseMetric(RoutingContext rc, long duration) {
    String namespace = (String) rc.data().get("namespace");
    String apiName = (String) rc.data().get("apiName");
    if (!Strings.isNullOrEmpty(apiName)
        && !Strings.isNullOrEmpty(namespace)) {
      try {
        ApiMetric.response(namespace, apiName, rc.response().getStatusCode(), duration);
      } catch (Exception e) {
        //ignore
      }
    }
  }

  private String mutiMapToString(MultiMap map, String defaultString) {
    StringBuilder s = new StringBuilder();
    for (String key : map.names()) {
      s.append(key)
              .append(":")
              .append(Joiner.on(",").join(map.getAll(key)))
              .append(";");
    }
    if (s.length() == 0) {
      return defaultString;
    }
    return s.toString();
  }

}