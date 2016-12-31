package com.edgar.direwolves.core.rpc.http;

import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Joiner;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edgar on 2016/12/30.
 *
 * @author Edgar  Date 2016/12/30
 */
public class HttpRpcHandler implements RpcHandler {

  private final HttpClient httpClient;

  HttpRpcHandler(Vertx vertx, JsonObject config) {
    this.httpClient = vertx.createHttpClient();
  }

  private boolean checkBody(HttpRpcRequest request) {
    return (request.getHttpMethod() == HttpMethod.POST
        || request.getHttpMethod() == HttpMethod.PUT)
        && request.getBody() == null;
  }

  private void header(HttpRpcRequest rpcRequest, HttpClientRequest request) {
    rpcRequest.getHeaders().asMap().forEach((headerName, headerValues) -> {
      request.putHeader(headerName, headerValues);
    });
  }

  private void exceptionHandler(Future<RpcResponse> future, HttpClientRequest request) {
    request.exceptionHandler(throwable -> {
      if (!future.isComplete()) {
        future.fail(throwable);
      }
    });
  }

  private void timeout(HttpRpcRequest rpcRequest, HttpClientRequest request) {
    if (rpcRequest.getTimeout() > 100) {
      request.setTimeout(rpcRequest.getTimeout());
    }
  }

  private void endRequest(HttpRpcRequest rpcRequest, HttpClientRequest request) {
    if (rpcRequest.getHttpMethod() == HttpMethod.GET) {
      request.end();
    } else if (rpcRequest.getHttpMethod() == HttpMethod.DELETE) {
      request.end();
    } else if (rpcRequest.getHttpMethod() == HttpMethod.POST) {
      request.setChunked(true)
          .end(rpcRequest.getBody().encode());
    } else if (rpcRequest.getHttpMethod() == HttpMethod.PUT) {
      request.setChunked(true)
          .end(rpcRequest.getBody().encode());
    }
  }

  private boolean checkMethod(HttpRpcRequest rpcRequest) {
    return rpcRequest.getHttpMethod() != HttpMethod.GET
        && rpcRequest.getHttpMethod() != HttpMethod.DELETE
        && rpcRequest.getHttpMethod() != HttpMethod.POST
        && rpcRequest.getHttpMethod() != HttpMethod.PUT;
  }

  private String requestPath(HttpRpcRequest rpcRequest) {
    List<String> query = new ArrayList<>(rpcRequest.getParams().size());
    for (String key : rpcRequest.getParams().keySet()) {
      String value = rpcRequest.getParams().get(key).iterator().next();
      if (value != null) {
        query.add(key + "=" + value);
      }
    }
    String queryString = Joiner.on("&").join(query);
    String path = rpcRequest.getPath();
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

  @Override
  public String type() {
    return "http";
  }

  @Override
  public Future<RpcResponse> handle(RpcRequest rpcRequest) {
    HttpRpcRequest httpRpcRequest = (HttpRpcRequest) rpcRequest;
    if (checkMethod(httpRpcRequest)) {
      return Future.failedFuture(
          SystemException.create(DefaultErrorCode.MISSING_ARGS).set("details", "method")
      );
    }
    if (checkBody(httpRpcRequest)) {
      return Future.failedFuture(
          SystemException.create(DefaultErrorCode.MISSING_ARGS).set("details", "body")
      );
    }
    Future<RpcResponse> future = Future.future();
    String path = requestPath(httpRpcRequest);
    final long startTime = System.currentTimeMillis();
    HttpClientRequest request =
        httpClient
            .request(httpRpcRequest.getHttpMethod(), httpRpcRequest.getPort(), httpRpcRequest.getHost(),
                path)
            .putHeader("content-type", "application/json");
    request.handler(response -> {
      response.bodyHandler(body -> {
        RpcResponse rpcResponse =
            RpcResponse.create(httpRpcRequest.getId(),
                response.statusCode(),
                body,
                System.currentTimeMillis() - startTime);
        future.complete(rpcResponse);
      }).exceptionHandler(throwable -> {
        if (!future.isComplete()) {
          future.fail(throwable);
        }
      });
    });
    header(httpRpcRequest, request);
    exceptionHandler(future, request);
    timeout(httpRpcRequest, request);

    endRequest(httpRpcRequest, request);
    return future;
  }
}
