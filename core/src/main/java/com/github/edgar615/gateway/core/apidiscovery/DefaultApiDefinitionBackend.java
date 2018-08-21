package com.github.edgar615.gateway.core.apidiscovery;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用本地map存储API.
 * 最初LocalMap中存储的是string，但是经过压测发现每次通过string转为ApiDefinition，对性能的影响较大。
 * 所以将ApiDefinition声明为Shareable，存储在LocalMap中(性能有显著提升)
 *
 * @author Edgar  Date 2017/6/20
 */
class DefaultApiDefinitionBackend implements ApiDefinitionBackend {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDefinitionBackend.class);

    private final LocalMap<String, ApiDefinition> registry;

    DefaultApiDefinitionBackend(Vertx vertx, String name) {
        this.registry = vertx.sharedData().<String, ApiDefinition>getLocalMap(name);
    }

    @Override
    public void store(ApiDefinition definition, Handler<AsyncResult<ApiDefinition>> resultHandler) {
        if (definition == null) {
            resultHandler.handle(Future.failedFuture("definition is null"));
            return;
        }
        registry.put(definition.name(), definition);
        resultHandler.handle(Future.succeededFuture(definition));
    }

    @Override
    public void remove(String name, Handler<AsyncResult<ApiDefinition>> resultHandler) {
        if (name == null) {
            resultHandler.handle(Future.failedFuture("name required"));
            return;
        }
        ApiDefinition definition = registry.remove(name);
        if (definition != null) {
            resultHandler.handle(Future.succeededFuture(definition));
        } else {
            resultHandler.handle(Future.failedFuture("Api: '" + name + "' not found"));
        }
    }

    @Override
    public void getDefinitions(Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(new ArrayList<>(registry.values())));
    }

    @Override
    public void getDefinition(String name, Handler<AsyncResult<ApiDefinition>> resultHandler) {
        if (name == null) {
            resultHandler.handle(Future.failedFuture("name required"));
            return;
        }
        ApiDefinition definition = registry.get(name);
        if (definition != null) {
            resultHandler.handle(Future.succeededFuture(definition));
        } else {
            resultHandler.handle(Future.failedFuture("Api: '" + name + "' not found"));
        }
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> resultHandler) {
        registry.clear();
        resultHandler.handle(Future.succeededFuture());
    }
}
