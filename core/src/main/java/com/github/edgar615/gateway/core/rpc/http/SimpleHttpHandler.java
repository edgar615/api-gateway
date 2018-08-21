package com.github.edgar615.gateway.core.rpc.http;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.rpc.RpcHandler;
import com.github.edgar615.gateway.core.rpc.RpcRequest;
import com.github.edgar615.gateway.core.rpc.RpcResponse;
import com.github.edgar615.gateway.core.utils.MultimapUtils;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
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
public class SimpleHttpHandler implements RpcHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpHandler.class);

    private final HttpClient httpClient;

    public SimpleHttpHandler(Vertx vertx, JsonObject config) {
        this.httpClient = vertx.createHttpClient();
    }

    //  @Override
    public Future<RpcResponse> handle(RpcRequest rpcRequest) {
        SimpleHttpRequest request = (SimpleHttpRequest) rpcRequest;
        if (checkMethod(request)) {
            return Future.failedFuture(
                    SystemException.create(DefaultErrorCode.INVALID_ARGS)
                            .set("details", "Method must be GET | POST | PUT | DELETE")
            );
        }
        if (checkBody(request)) {
            return Future.failedFuture(
                    SystemException.create(DefaultErrorCode.MISSING_ARGS)
                            .set("details", "POST or PUT method must contains request body")
            );
        }

        logClientSend(request);
        Future<RpcResponse> future = Future.future();
        String path = requestPath(request);
        final Duration duration = new Duration();
        HttpClientRequest httpClientRequest =
                httpClient
                        .request(request.method(), request.port(), request.host(), path)
                        .putHeader("content-type", "application/json; charset=utf-8");
        httpClientRequest.handler(response -> {
            duration.setRepliedOn(System.currentTimeMillis());
            response.bodyHandler(body -> {
                duration.setBodyHandledOn(System.currentTimeMillis());
                RpcResponse rpcResponse =
                        RpcResponse.create(request.id(),
                                           response.statusCode(),
                                           body,
                                           duration.duration());
                logClientReceived(rpcResponse, body, duration);
                future.complete(rpcResponse);
            }).exceptionHandler(throwable -> {
                if (!future.isComplete()) {
                    logRequestError(rpcRequest, duration, throwable);
                    future.fail(throwable);
                }
            });
        }).connectionHandler(conn -> {
            duration.setConnectedOn(System.currentTimeMillis());
        }).endHandler(v -> {
            duration.setEndedOn(System.currentTimeMillis());
        });
        header(request, httpClientRequest);
        exceptionHandler(future, httpClientRequest);
        timeout(request, httpClientRequest);

        endRequest(request, httpClientRequest);
        return future;
    }

    @Override
    public String type() {
        return SimpleHttpEndpoint.TYPE;
    }

    private String urlEncode(String path) {
        try {
            return URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return path;
        }
    }

    private boolean checkBody(HttpRpcRequest request) {
        return (request.method() == HttpMethod.POST
                || request.method() == HttpMethod.PUT)
               && request.body() == null;
    }

    private void header(HttpRpcRequest rpcRequest, HttpClientRequest request) {
        rpcRequest.headers().asMap().forEach((headerName, headerValues) -> {
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
        if (rpcRequest.timeout() > 100) {
            request.setTimeout(rpcRequest.timeout());
        }
    }

    private void endRequest(HttpRpcRequest rpcRequest, HttpClientRequest request) {
        if (rpcRequest.method() == HttpMethod.GET) {
            request.end();
        } else if (rpcRequest.method() == HttpMethod.DELETE) {
            request.end();
        } else if (rpcRequest.method() == HttpMethod.POST) {
            request.setChunked(true)
                    .end(rpcRequest.body().encode());
        } else if (rpcRequest.method() == HttpMethod.PUT) {
            request.setChunked(true)
                    .end(rpcRequest.body().encode());
        }
    }

    private boolean checkMethod(HttpRpcRequest rpcRequest) {
        return rpcRequest.method() != HttpMethod.GET
               && rpcRequest.method() != HttpMethod.DELETE
               && rpcRequest.method() != HttpMethod.POST
               && rpcRequest.method() != HttpMethod.PUT;
    }

    private String requestPath(HttpRpcRequest rpcRequest) {
        List<String> query = new ArrayList<>(rpcRequest.params().size());
        for (String key : rpcRequest.params().keySet()) {
            String value = rpcRequest.params().get(key).iterator().next();
            if (value != null) {
                query.add(key + "=" + urlEncode(value));
            }
        }
        String queryString = Joiner.on("&").join(query);
        String path = rpcRequest.path();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!Strings.isNullOrEmpty(queryString)) {
            if (path.indexOf("?") > 0) {
                path += "&" + queryString;
            } else {
                path += "?" + queryString;
            }
        }
        return path;
    }

    private void logRequestError(RpcRequest rpcRequest, Duration duration, Throwable throwable) {

        LOGGER.error("[{}] [CR] [HTTP] [{}ms]", rpcRequest.id(),
                     duration.getRepliedOn() - duration.getCreatedon(), throwable);
    }

    private void logClientReceived(RpcResponse rpcResponse, Buffer body, Duration duration) {
        LOGGER.info("[{}] [CR] [HTTP] [{}] [{}bytes] [{}ms]", rpcResponse.id(),
                    rpcResponse.statusCode(),
                    body.getBytes().length,
                    rpcResponse.elapsedTime());
    }

    private void logClientSend(HttpRpcRequest httpRpcRequest) {
        LOGGER.info("[{}] [CS] [HTTP] [{}:{}] [{} {}] [{}] [{}] [{}]", httpRpcRequest.id(),
                    httpRpcRequest.host(), httpRpcRequest.port(),
                    httpRpcRequest.method().name(), httpRpcRequest.path(),
                    MultimapUtils.convertToString(httpRpcRequest.headers(), "no header"),
                    MultimapUtils.convertToString(httpRpcRequest.params(), "no param"),
                    httpRpcRequest.body() == null ? "no body" : httpRpcRequest.body().encode());
    }

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
