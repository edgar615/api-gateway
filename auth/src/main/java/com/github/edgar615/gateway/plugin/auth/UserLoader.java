package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.vertx.cache.CacheLoader;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Administrator on 2017/10/27.
 */
class UserLoader implements CacheLoader<String, JsonObject> {

    private final Vertx vertx;

    private final int port;

    private final String path;

    UserLoader(Vertx vertx, int port, String path) {
        this.vertx = vertx;
        this.port = port;
        this.path = path;
    }

    @Override
    public void load(String key, Handler<AsyncResult<JsonObject>> handler) {
        String requestURI = this.path;
        if (!requestURI.startsWith("/")) {
            requestURI = "/" + requestURI;
        }
        if (requestURI.contains("?")) {
            requestURI = requestURI + "&userId=" + key;
        } else {
            requestURI = requestURI + "?userId=" + key;
        }
        vertx.createHttpClient().get(port, "127.0.0.1", requestURI, response -> {
            if (response.statusCode() >= 400) {
                SystemException e = SystemException.create(DefaultErrorCode.PERMISSION_DENIED)
                        .set("details", "Undefined User:" + key);
                handler.handle(Future.failedFuture(e));
            } else {
                response.handler(body -> {
                    handler.handle(Future.succeededFuture(body.toJsonObject()));
                });
            }
        }).exceptionHandler(e -> handler.handle(Future.failedFuture(e)))
                .end();
    }

}
