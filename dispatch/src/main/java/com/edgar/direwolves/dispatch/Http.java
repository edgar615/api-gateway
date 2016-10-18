package com.edgar.direwolves.dispatch;

import com.google.common.base.Joiner;

import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edgar on 2016/9/21.
 *
 * @author Edgar  Date 2016/9/21
 */
public class Http {

  private static HttpMethod checkMethod(String method) {
    try {
      return HttpMethod.valueOf(method.toUpperCase());
    } finally {
      new UnsupportedOperationException("method is " + method);
    }
  }


  public static Future<HttpResult> request(HttpClient httpClient, JsonObject config) {
    HttpMethod method = null;
    try {
      method = HttpMethod.valueOf(config.getString("method", "GET").toUpperCase());
    } catch (Exception e) {
      return Future.failedFuture(e);
    }

//    if (checkBody(options)) {
//      return Future.failedFuture(
//              new UnsupportedOperationException("body is null," + options.getHttpMethod()));
//    }
    Future<HttpResult> future = Future.future();
    String path = requestPath(config);
    String id = config.getString("id");
    final long startTime = System.currentTimeMillis();
    HttpClientRequest request =
            httpClient.request(method,
                               config.getInteger("port", 80),
                               config.getString("host", "localhost"),
                               path)
                    .putHeader("content-type", "application/json");

    request.handler(response -> {
      response.bodyHandler(body -> {
        HttpResult httpResult = HttpResult.create(id,
                                                  response.statusCode(),
                                                  body,
                                                  System.currentTimeMillis() - startTime);
        future.complete(httpResult);
      }).exceptionHandler(throwable -> {
        if (!future.isComplete()) {
          future.fail(throwable);
        }
      });
    });
    header(config, request);
    exceptionHandler(future, request);
    timeout(config, request);

    endRequest(config, request);
    return future;
  }


  private static void header(JsonObject config, HttpClientRequest request) {
    JsonObject headerConfig = config.getJsonObject("headers", new JsonObject());
    for (String key : headerConfig.fieldNames()) {
      Object value = headerConfig.getValue(key);
      if (value instanceof JsonArray) {
        JsonArray array = (JsonArray) value;
        for (int i = 0; i < array.size(); i++) {
          request.putHeader(key, array.getValue(i).toString());
        }
      } else {
        request.putHeader(key, value.toString());
      }
    }
  }

  private static void exceptionHandler(Future<HttpResult> future, HttpClientRequest request) {
    request.exceptionHandler(throwable -> {
      if (!future.isComplete()) {
        future.fail(throwable);
      }
    });
  }

  private static void timeout(JsonObject config, HttpClientRequest request) {
    int timeout = config.getInteger("timeout", 0);
    if (timeout > 0) {
      request.setTimeout(timeout);
    }
  }

  private static void endRequest(JsonObject config, HttpClientRequest request) {
    String method = config.getString("method", "GET");
    if ("GET".equalsIgnoreCase(method)
        || "DELETE".equalsIgnoreCase(method)) {
      request.end();
    } else if ("POST".equalsIgnoreCase(method)
               || "PUT".equalsIgnoreCase(method)) {
      request.setChunked(true)
              .end(config.getJsonObject("body", new JsonObject()).encode());
    }
  }

  private static String requestPath(JsonObject jsonObject) {
    JsonObject params = jsonObject.getJsonObject("params", new JsonObject());
    List<String> query = new ArrayList<>(params.size());
    for (String key : params.fieldNames()) {
      Object value = params.getValue(key);
      if (value instanceof JsonArray) {
        JsonArray array = (JsonArray) value;
        for (int i = 0; i < array.size(); i++) {
          query.add(key + "=" + array.getValue(i).toString());
        }
      } else {
        query.add(key + "=" + value.toString());
      }
    }
    String queryString = Joiner.on("&").join(query);
    String path = jsonObject.getString("path", "/");
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    if (path.indexOf("?") > 0) {
      path += "&" + queryString;
    } else {
      path += "?" + queryString;
    }
    return path;
  }

}
