package com.edgar.direwolves.loadbalance;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edgar on 2016/12/30.
 *
 * @author Edgar  Date 2016/12/30
 */
@Deprecated
public class HttpExecutable {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpExecutable.class);

  private final HttpClient httpClient;


  protected HttpExecutable(Vertx vertx, JsonObject config) {
    this.httpClient = vertx.createHttpClient();
  }

//  //  @Override
//  public Future<RpcResponse> handle(HttpRequest request) {
//    if (checkMethod(request)) {
//      return Future.failedFuture(
//              SystemException.create(DefaultErrorCode.INVALID_ARGS)
//                      .set("details", "Method must be GET | POST | PUT | DELETE")
//      );
//    }
//    if (checkBody(request)) {
//      return Future.failedFuture(
//              SystemException.create(DefaultErrorCode.MISSING_ARGS)
//                      .set("details", "POST or PUT method must contains request body")
//      );
//    }
//
//    String host = request.record().getLocation().getString("host");
//    int port = request.record().getLocation().getInteger("port");
//
//    Log.create(LOGGER)
//            .setTraceId(request.id())
//            .setLogType(LogType.CS)
//            .setEvent("HTTP")
//            .addData("service", request.record().getName())
//            .addData("server", host + ":" + port)
//            .setMessage("[{}] [{}] [{}] [{}]")
//            .addArg(request.method().name() + " " + request.path())
//            .addArg(MultimapUtils.convertToString(request.headers(), "no header"))
//            .addArg(MultimapUtils.convertToString(request.params(), "no param"))
//            .addArg(request.body() == null ? "no body" : request.body().encode())
//            .info();
//
//
//    Future<RpcResponse> future = Future.future();
//    String path = requestPath(request);
//    final Duration duration = new Duration();
//    HttpClientRequest httpClientRequest =
//            httpClient
//                    .request(request.method(), port, host, path)
//                    .putHeader("content-type", "application/json");
//    httpClientRequest.handler(response -> {
//      duration.setRepliedOn(System.currentTimeMillis());
//      response.bodyHandler(body -> {
//        duration.setBodyHandledOn(System.currentTimeMillis());
//        RpcResponse rpcResponse =
//                RpcResponse.create(request.id(),
//                                   response.statusCode(),
//                                   body,
//                                   duration.duration());
//        Log.create(LOGGER)
//                .setTraceId(request.id())
//                .setLogType(LogType.CR)
//                .setEvent("HTTP")
//                .addData("ct", duration.getCreatedon() == 0 ? 0 : duration.getCreatedon() -
//                                                                  duration.getCreatedon())
//                .addData("et", duration.getEndedOn() == 0 ? 0 :
//                        duration.getEndedOn() - duration.getCreatedon())
//                .addData("rt", duration.getRepliedOn() == 0 ? 0 :
//                        duration.getRepliedOn() - duration.getCreatedon())
//                .addData("bt", duration.getBodyHandledOn() == 0 ? 0 :
//                        duration.getBodyHandledOn() - duration.getCreatedon())
//                .setMessage(" [{}] [{}ms] [{} bytes]")
//                .addArg(rpcResponse.statusCode())
//                .addArg(rpcResponse.elapsedTime())
//                .addArg(body.getBytes().length)
//                .info();
//        future.complete(rpcResponse);
//      }).exceptionHandler(throwable -> {
//        if (!future.isComplete()) {
//          Log.create(LOGGER)
//                  .setTraceId(request.id())
//                  .setLogType(LogType.CR)
//                  .setEvent("HTTP")
//                  .addData("ct", duration.getCreatedon() == 0 ? 0 : duration.getCreatedon() -
//                                                                    duration.getCreatedon())
//                  .addData("et", duration.getEndedOn() == 0 ? 0 :
//                          duration.getEndedOn() - duration.getCreatedon())
//                  .addData("rt", duration.getRepliedOn() == 0 ? 0 :
//                          duration.getRepliedOn() - duration.getCreatedon())
//                  .addData("bt", duration.getBodyHandledOn() == 0 ? 0 :
//                          duration.getBodyHandledOn() - duration.getCreatedon())
//                  .setThrowable(throwable)
//                  .error();
//          future.fail(throwable);
//        }
//      });
//    }).connectionHandler(conn -> {
//      duration.setConnectedOn(System.currentTimeMillis());
//    }).endHandler(v -> {
//      duration.setEndedOn(System.currentTimeMillis());
//    });
//    header(request, httpClientRequest);
//    exceptionHandler(future, httpClientRequest, request.record().getRegistration());
//    timeout(request, httpClientRequest);
//
//    endRequest(request, httpClientRequest);
//    return future;
//  }
//
//  public String urlEncode(String path) {
//    try {
//      return URLEncoder.encode(path, "UTF-8");
//    } catch (UnsupportedEncodingException e) {
//      return path;
//    }
//  }
//
//  private boolean checkBody(HttpRequest request) {
//    return (request.method() == HttpMethod.POST
//            || request.method() == HttpMethod.PUT)
//           && request.body() == null;
//  }
//
//  private void header(HttpRequest rpcRequest, HttpClientRequest request) {
//    rpcRequest.headers().asMap().forEach((headerName, headerValues) -> {
//      request.putHeader(headerName, headerValues);
//    });
//  }
//
//  private void exceptionHandler(Future<RpcResponse> future, HttpClientRequest request,
//                                String serverId) {
//    request.exceptionHandler(throwable -> {
//      if (!future.isComplete()) {
////        if (metric != null) {
////          metric.failed(serverId);
////        }
//        future.fail(throwable);
//      }
//    });
//  }
//
//  private void timeout(HttpRequest rpcRequest, HttpClientRequest request) {
//    if (rpcRequest.timeout() > 100) {
//      request.setTimeout(rpcRequest.timeout());
//    }
//  }
//
//  private void endRequest(HttpRequest rpcRequest, HttpClientRequest request) {
//    if (rpcRequest.method() == HttpMethod.GET) {
//      request.end();
//    } else if (rpcRequest.method() == HttpMethod.DELETE) {
//      request.end();
//    } else if (rpcRequest.method() == HttpMethod.POST) {
//      request.setChunked(true)
//              .end(rpcRequest.body().encode());
//    } else if (rpcRequest.method() == HttpMethod.PUT) {
//      request.setChunked(true)
//              .end(rpcRequest.body().encode());
//    }
//  }
//
//  private boolean checkMethod(HttpRequest rpcRequest) {
//    return rpcRequest.method() != HttpMethod.GET
//           && rpcRequest.method() != HttpMethod.DELETE
//           && rpcRequest.method() != HttpMethod.POST
//           && rpcRequest.method() != HttpMethod.PUT;
//  }

//  private String requestPath(HttpRequest rpcRequest) {
//    List<String> query = new ArrayList<>(rpcRequest.params().size());
//    for (String key : rpcRequest.params().keySet()) {
//      String value = rpcRequest.params().get(key).iterator().next();
//      if (value != null) {
//        query.add(key + "=" + urlEncode(value));
//      }
//    }
//    String queryString = Joiner.on("&").join(query);
//    String path = rpcRequest.path();
//    if (!path.startsWith("/")) {
//      path = "/" + path;
//    }
//    if (!Strings.isNullOrEmpty(queryString)) {
//      if (path.indexOf("?") > 0) {
//        path += "&" + queryString;
//      } else {
//        path += "?" + queryString;
//      }
//    }
//    return path;
//  }

  /**
   * 为了更准确的度量http请求的性能，需要记录HTTP各个阶段的时间。
   */
  private class Duration {

    /**
     * 创建时间
     */
    private long createdon = System.currentTimeMillis();

    /**
     * 发送请求时间
     */
    private long endedOn;

    /**
     * 连接时间
     */
    private long connectedOn;

    /**
     * 服务端响应时间
     */
    private long repliedOn;

    /**
     * body处理时间：body处理会比收到response慢一点点
     */
    private long bodyHandledOn;

    public long duration() {
      return bodyHandledOn - createdon;
    }

    public long getCreatedon() {
      return createdon;
    }

    public void setCreatedon(long createdon) {
      this.createdon = createdon;
    }

    public long getEndedOn() {
      return endedOn;
    }

    public void setEndedOn(long endedOn) {
      this.endedOn = endedOn;
    }

    public long getConnectedOn() {
      return connectedOn;
    }

    public void setConnectedOn(long connectedOn) {
      this.connectedOn = connectedOn;
    }

    public long getRepliedOn() {
      return repliedOn;
    }

    public void setRepliedOn(long repliedOn) {
      this.repliedOn = repliedOn;
    }

    public long getBodyHandledOn() {
      return bodyHandledOn;
    }

    public void setBodyHandledOn(long bodyHandledOn) {
      this.bodyHandledOn = bodyHandledOn;
    }
  }
}
