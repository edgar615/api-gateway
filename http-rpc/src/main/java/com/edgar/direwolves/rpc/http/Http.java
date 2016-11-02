package com.edgar.direwolves.rpc.http;

import com.google.common.base.Joiner;

import com.edgar.direwolves.core.rpc.HttpResult;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Edgar on 2016/9/21.
 *
 * @author Edgar  Date 2016/9/21
 */
public class Http {

  public static Future<HttpResult> request(HttpClient httpClient, HttpRequestOptions options) {
    if (checkMethod(options)) {
      return Future.failedFuture(
              SystemException.create(DefaultErrorCode.MISSING_ARGS).set("details", "method")
      );
    }
    if (checkBody(options)) {
      return Future.failedFuture(
              SystemException.create(DefaultErrorCode.MISSING_ARGS).set("details", "body")
      );
    }
    Future<HttpResult> future = Future.future();
    String path = requestPath(options);
    final long startTime = System.currentTimeMillis();
    HttpClientRequest request =
            httpClient.request(options.getHttpMethod(), options.getPort(), options.getHost(), path)
                    .putHeader("content-type", "application/json");
    request.handler(response -> {
      response.bodyHandler(body -> {
        HttpResult httpResult =
                HttpResult.create(options.getId(),
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
    header(options, request);
    exceptionHandler(future, request);
    timeout(options, request);

    endRequest(options, request);
    return future;
  }

  private static boolean checkBody(HttpRequestOptions options) {
    return (options.getHttpMethod() == HttpMethod.POST
            || options.getHttpMethod() == HttpMethod.PUT)
           && options.getBody() == null;
  }

  private static void header(HttpRequestOptions options, HttpClientRequest request) {
    options.getHeader().asMap().forEach((headerName, headerValues) -> {
      request.putHeader(headerName, headerValues);
    });
  }

  private static void exceptionHandler(Future<HttpResult> future, HttpClientRequest request) {
    request.exceptionHandler(throwable -> {
      if (!future.isComplete()) {
        future.fail(throwable);
      }
    });
  }

  private static void timeout(HttpRequestOptions options, HttpClientRequest request) {
    if (options.getTimeout() > 100) {
      request.setTimeout(options.getTimeout());
    }
  }

  private static void endRequest(HttpRequestOptions options, HttpClientRequest request) {
    if (options.getHttpMethod() == HttpMethod.GET) {
      request.end();
    } else if (options.getHttpMethod() == HttpMethod.DELETE) {
      request.end();
    } else if (options.getHttpMethod() == HttpMethod.POST) {
      request.setChunked(true)
              .end(options.getBody().encode());
    } else if (options.getHttpMethod() == HttpMethod.PUT) {
      request.setChunked(true)
              .end(options.getBody().encode());
    }
  }

  private static boolean checkMethod(HttpRequestOptions options) {
    return options.getHttpMethod() != HttpMethod.GET
           && options.getHttpMethod() != HttpMethod.DELETE
           && options.getHttpMethod() != HttpMethod.POST
           && options.getHttpMethod() != HttpMethod.PUT;
  }

  private static String requestPath(HttpRequestOptions options) {
    List<String> query = new ArrayList<>(options.getParams().size());
    for (String key : options.getParams().keySet()) {
      Collection<String> value = options.getParams().get(key);
      if (value != null) {
        for (String val : value) {
          query.add(key + "=" + val);
        }
      }
    }
    String queryString = Joiner.on("&").join(query);
    String path = options.getPath();
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