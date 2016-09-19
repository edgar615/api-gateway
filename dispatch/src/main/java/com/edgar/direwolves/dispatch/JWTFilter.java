package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.definition.AuthDefinition;
import com.edgar.direwolves.definition.AuthDefinitionRegistry;
import com.edgar.direwolves.definition.AuthType;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Strings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edgar on 16-9-18.
 */
public class JWTFilter implements Filter {

    private static final String HEADER_AUTH = "Authorization";

    private static final String AUTH_PREFIX = "Bearer ";

    private static final String TYPE = "jwt";

    private JsonObject config = new JsonObject()
            .put("path", "keystore.jceks")
            .put("type", "jceks")//JKS, JCEKS, PKCS12, BKS，UBER
            .put("password", "secret");

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void config(JsonObject config) {
        this.config.mergeIn(config);
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        List<AuthDefinition> definitions = AuthDefinitionRegistry.create()
                .filter(apiContext.apiName(), AuthType.JWT);
        return definitions.size() == 1;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> nextFuture) {
        //token
        String token = extractToken(apiContext);
    }

    /**
     * 从header中提取token信息.
     *
     * @param apiContext
     */
    private String extractToken(ApiContext apiContext) {
        if (apiContext.headers().containsKey(HEADER_AUTH)) {
            List<String> authorizationHeaders = new ArrayList<>(apiContext.headers().get(HEADER_AUTH));
            String authorization = authorizationHeaders.get(0);
            if (!Strings.isNullOrEmpty(authorization) && authorization.startsWith(AUTH_PREFIX)) {
                return authorization.substring(AUTH_PREFIX.length());
            }
        }
        throw SystemException.create(DefaultErrorCode.INVALID_TOKEN);
    }

    private Future<User> auth(Vertx vertx, String token) {

        JsonObject jwtConfig = new JsonObject().put("keyStore", config);

        JWTAuth provider = JWTAuth.create(vertx, jwtConfig);

        Future<User> authFuture = Future.future();
        provider.authenticate(new JsonObject().put("jwt", token), ar -> {
            if (ar.succeeded()) {
                authFuture.complete(ar.result());
            } else {
                String errorMessage = ar.cause().getMessage();
                if (errorMessage != null) {
                    if (errorMessage.startsWith("Expired JWT token")) {
                        authFuture.fail(SystemException.wrap(DefaultErrorCode.EXPIRE_TOKEN, ar.cause()));
                    } else if (errorMessage.startsWith("Invalid JWT token")) {
                        authFuture.fail(SystemException.wrap(DefaultErrorCode.INVALID_TOKEN, ar.cause()));
                    } else {
                        authFuture.fail(SystemException.wrap(DefaultErrorCode.NO_AUTHORITY, ar.cause()));
                    }
                } else {
                    authFuture.fail(SystemException.wrap(DefaultErrorCode.NO_AUTHORITY, ar.cause()));
                }
            }
        });
        return authFuture;
    }

}
