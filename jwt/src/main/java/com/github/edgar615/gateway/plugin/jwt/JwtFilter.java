package com.github.edgar615.gateway.plugin.jwt;

import com.google.common.base.Strings;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * 身份认证.
 * 目前仅支持JWT类型的认证
 * 在校验通过之后，会在上下文中存入用户信息:
 * * 该filter可以接受下列的配置参数
 * <pre>
 * "jwt.auth": {
 * "ignoreExpiration": false, 是否校验exp 可选，默认false
 * "audiences": [], 校验aud，JSON数组，可选
 * "issuer": "", 校验iss，可选
 * "leeway": 0 允许调用方与服务端的偏差
 * }
 * </pre>
 * 该filter的order=1000
 *
 * @author Edgar  Date 2016/10/31
 */
public class JwtFilter implements Filter {

    private static final String AUTH_HEADER = "Authorization";

    private static final String HEADER_PREFIX = "Bearer ";

    private final String userKey = "userId";

    private final Vertx vertx;

    private final JWTAuthOptions jwtAuthOptions;

    private final String headerName;

    private final String prefix;

    JwtFilter(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        JsonObject tokenConfig = config.getJsonObject("token", new JsonObject());
        this.prefix = tokenConfig.getString("prefix", HEADER_PREFIX);
        this.headerName = tokenConfig.getString("headerName", AUTH_HEADER);
        if (config.getValue("jwt.auth") instanceof JsonObject) {
            JsonObject jwtConfig = config.getJsonObject("jwt.auth");
            this.jwtAuthOptions = new JWTAuthOptions(jwtConfig);
        } else {
            this.jwtAuthOptions = new JWTAuthOptions();
        }

        if (config.getValue("keyStore") instanceof JsonObject) {
            this.jwtAuthOptions
                    .setKeyStore(new KeyStoreOptions(config.getJsonObject("keyStore")));
        } else {
            KeyStoreOptions keyStoreOptions = new KeyStoreOptions()
                    .setPath("keystore.jceks")
                    .setType("jceks")
                    .setPassword("INIHPMOZPO");
            this.jwtAuthOptions.setKeyStore(keyStoreOptions);
        }
    }

    @Override
    public String type() {
        return PRE;
    }

    @Override
    public int order() {
        return 10000;
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        return apiContext.apiDefinition()
                       .plugin(JwtPlugin.class.getSimpleName()) != null
               && apiContext.principal() == null;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
        try {
            String token = extractToken(apiContext);
            Future<JsonObject> authFuture = auth(token);
            authFuture
                    .setHandler(ar -> {
                        if (ar.succeeded()) {
                            JsonObject principal = ar.result();
                            apiContext.setPrincipal(principal);
                            completeFuture.complete(apiContext);
                        } else {
                            failed(completeFuture, apiContext.id(), "AuthFailure", ar.cause());
                        }
                    });
        } catch (Exception e) {
            failed(completeFuture, apiContext.id(), "AuthFailure", e);
        }
    }


    /**
     * 从header中提取token信息.
     *
     * @param apiContext
     */
    private String extractToken(ApiContext apiContext) {
        if (apiContext.headers().containsKey(headerName)) {
            List<String> authorizationHeaders =
                    new ArrayList<>(apiContext.headers().get(headerName));
            String authorization = authorizationHeaders.get(0);
            if (!Strings.isNullOrEmpty(authorization) && authorization.startsWith(prefix)) {
                return authorization.substring(prefix.length()).trim();
            } else {
                throw SystemException.create(DefaultErrorCode.INVALID_TOKEN)
                        .set("details", String.format("The format of the token: %s:%s<token>",
                                                      headerName, prefix));
            }
        }
        throw SystemException.create(DefaultErrorCode.INVALID_REQ)
                .set("details", "Miss rquest header: " + headerName);
    }

    private Future<JsonObject> auth(String token) {
        Future<JsonObject> authFuture = Future.future();
        JWTAuth provider = JWTAuth.create(vertx, jwtAuthOptions);
        provider.authenticate(new JsonObject().put("jwt", token), ar -> {
            if (ar.succeeded()) {
                JsonObject principal = ar.result().principal();
                if (principal.containsKey(userKey)) {
                    authFuture.complete(principal);
                } else {
                    SystemException systemException =
                            SystemException.create(DefaultErrorCode.INVALID_TOKEN)
                                    .set("details", "no " + userKey);
                    authFuture.fail(systemException);
                }

            } else {
                fail(authFuture, ar);
            }
        });
        return authFuture;
    }

    private void fail(Future<JsonObject> completeFuture, AsyncResult<User> ar) {
        String errorMessage = ar.cause().getMessage();
        if (errorMessage != null) {
            if (errorMessage.startsWith("Expired JWT")) {
                completeFuture.fail(SystemException.wrap(DefaultErrorCode.EXPIRE_TOKEN, ar.cause())
                                            .set("details", errorMessage));
            } else if (errorMessage.startsWith("Invalid JWT")) {
                completeFuture.fail(SystemException.wrap(DefaultErrorCode.INVALID_TOKEN, ar.cause())
                                            .set("details", errorMessage));
            } else {
                completeFuture
                        .fail(SystemException.wrap(DefaultErrorCode.PERMISSION_DENIED, ar.cause())
                                      .set("details", ar.cause().getMessage()));
            }
        } else {
            completeFuture.fail(SystemException.wrap(DefaultErrorCode.PERMISSION_DENIED, ar.cause())
                                        .set("details", ar.cause().getMessage()));
        }
    }

}
