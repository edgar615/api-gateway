package com.edgar.direwolves.core.utils;

import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import org.slf4j.Logger;

/**
 * Created by Edgar on 2017/3/15.
 *
 * @author Edgar  Date 2017/3/15
 */
public class Helper {

  public static void log(Logger logger, ApiContext apiContext) {
    StringBuilder log = new StringBuilder(apiContext.id())
            .append("\n")
            .append(logReq(apiContext))
            .append("\n")
            .append(logResult(apiContext));
    logger.info(log.toString());
  }

  public static StringBuilder logResult(ApiContext apiContext) {
    StringBuilder log = new StringBuilder();
    long started = (long) apiContext.variables()
            .getOrDefault("request.time", System.currentTimeMillis());
    long ended = System.currentTimeMillis();
    long duration = ended - started;
    log.append("<--- HTTP ")
            .append(apiContext.result().statusCode())
            .append(" ")
            .append(duration)
            .append("ms")
            .append("\n");
    Multimap<String, String> headers = apiContext.result().header();
    for (String key : headers.keySet()) {
      log.append(key)
              .append(" : ")
              .append(headers.get(key))
              .append("\n");
    }
    if (apiContext.result().responseObject() != null) {
      log.append(apiContext.result().responseObject().encode());
    } else if (apiContext.result().responseArray() != null) {
      log.append(apiContext.result().responseArray().encode());
    } else {
      log.append("(nobody)");
    }
    log.append("\n")
            .append("<--- END ")
            .append(apiContext.result().byteSize())
            .append("bytes");
    return log;
  }

  public static StringBuilder logReq(ApiContext apiContext) {
    StringBuilder log = new StringBuilder();
    log.append("---> HTTP  (SUCCEED) ")
            .append("\n")
            .append(apiContext.method().name())
            .append(" ")
            .append(apiContext.path())
            .append("\n");
    Multimap<String, String> headers = apiContext.headers();
    for (String key : headers.keySet()) {
      log.append(key)
              .append(" : ")
              .append(headers.get(key))
              .append("\n");
    }
    Multimap<String, String> params = apiContext.params();
    for (String key : params.keySet()) {
      log.append(key)
              .append(" : ")
              .append(params.get(key))
              .append("\n");
    }
    if (apiContext.body() != null) {
      log.append(apiContext.body().encode());
    } else {
      log.append("(nobody)");
    }
    log.append("\n").append("---> END");
    return log;
  }

//  public String toString() {
//    MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("ApiContext")
//            .add("id", id)
//            .add("params", params)
//            .add("headers", headers)
//            .add("body", body)
//            .add("variables", variables)
//            .add("apiDefinition", apiDefinition);
//    if (principal != null) {
//      helper.add("principal", principal.encode());
//    }
//    helper.add("requests", requests);
//    helper.add("responses", responses);
//    helper.add("result", result);
//
//    return helper.toString();
//  }
}
