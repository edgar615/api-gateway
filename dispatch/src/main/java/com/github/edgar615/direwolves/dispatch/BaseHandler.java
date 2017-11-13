package com.github.edgar615.direwolves.dispatch;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.github.edgar615.direwolves.core.metric.ApiMetric;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.log.LogType;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.TimeZone;
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
    Log.create(LOGGER)
            .setTraceId(id)
            .setLogType(LogType.SR)
            .setEvent("http.request.received")
            .setMessage("[{} {}] [{}] [{}] [{}]")
            .addArg(rc.request().method().name())
            .addArg(rc.normalisedPath())
            .addArg(mutiMapToString(rc.request().headers(), "no header"))
            .addArg(mutiMapToString(rc.request().params(), "no param"))
            .addArg((rc.getBody() == null || rc.getBody().length() == 0) ? "no body" : rc.getBody()
                    .toString())
            .info();


    rc.addHeadersEndHandler(v -> {
      rc.response().putHeader("x-server-time",
                              ISO8601Utils.format(new Date(), false, TimeZone.getDefault()));
    });

    rc.addBodyEndHandler(v -> {
      long duration = System.currentTimeMillis() - start;
      Log.create(LOGGER)
              .setTraceId(id)
              .setLogType(LogType.SS)
              .setEvent("http.request.reply")
              .setMessage(" [{}] [{}] [{}ms] [{} bytes]")
              .addArg(rc.response().getStatusCode())
              .addArg(mutiMapToString(rc.response().headers(), "no header"))
              .addArg(duration)
              .addArg(rc.response().bytesWritten())
              .info();
      responseMetric(rc, duration);
    });
    rc.next();
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