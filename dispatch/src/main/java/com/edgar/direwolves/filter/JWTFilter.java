package com.edgar.direwolves.filter;

import com.edgar.direwolves.definition.AuthDefinition;
import com.edgar.direwolves.definition.AuthDefinitionRegistry;
import com.edgar.direwolves.definition.AuthType;
import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Strings;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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

    private Vertx vertx;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void config(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        if (config.containsKey("keystore.path")) {
            this.config.put("path", config.getString("keystore.path"));
        }
        if (config.containsKey("keystore.type")) {
            this.config.put("type", config.getString("keystore.type"));
        }
        if (config.containsKey("keystore.password")) {
            this.config.put("password", config.getString("keystore.password"));
        }
        if (config.containsKey("jwt.alg")) {
            this.config.put("algorithm", config.getString("jwt.alg"));
        }
        if (config.containsKey("jwt.audience")) {
            this.config.put("audience", config.getString("jwt.audience"));
        }
        if (config.containsKey("jwt.issuer")) {
            this.config.put("issuer", config.getString("jwt.issuer"));
        }
        if (config.containsKey("jwt.subject")) {
            this.config.put("subject", config.getString("jwt.subject"));
        }
        if (config.containsKey("jwt.expires")) {
            this.config.put("expiresInSeconds", config.getInteger("jwt.expires"));
        }
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        List<AuthDefinition> definitions = AuthDefinitionRegistry.create()
                .filter(apiContext.apiName(), AuthType.JWT);
        return definitions.size() == 1;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
        //token
        String token = extractToken(apiContext);
        auth(token, apiContext, completeFuture);
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

    private void auth(String token, ApiContext apiContext, Future<ApiContext> completeFuture) {

        JsonObject jwtConfig = new JsonObject().put("keyStore", config);

        JWTAuth provider = JWTAuth.create(vertx, jwtConfig);

        provider.authenticate(new JsonObject().put("jwt", token), ar -> {
            if (ar.succeeded()) {
                apiContext.setPrincipal(ar.result().principal());
                completeFuture.complete(apiContext);
            } else {
                String errorMessage = ar.cause().getMessage();
                if (errorMessage != null) {
                    if (errorMessage.startsWith("Expired JWT token")) {
                        completeFuture.fail(SystemException.wrap(DefaultErrorCode.EXPIRE_TOKEN, ar.cause()));
                    } else if (errorMessage.startsWith("Invalid JWT token")) {
                        completeFuture.fail(SystemException.wrap(DefaultErrorCode.INVALID_TOKEN, ar.cause()));
                    } else {
                        completeFuture.fail(SystemException.wrap(DefaultErrorCode.NO_AUTHORITY, ar.cause()));
                    }
                } else {
                    completeFuture.fail(SystemException.wrap(DefaultErrorCode.NO_AUTHORITY, ar.cause()));
                }
            }
        });
    }

}
