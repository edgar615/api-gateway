package com.github.edgar615.gateway.core.apidiscovery;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
class DefaultApiDefinitionBackend implements ApiDefinitionBackend {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDefinitionBackend.class);

    private final List<ApiDefinition> registry = new CopyOnWriteArrayList<>();

    @Override
    public void store(ApiDefinition definition, Handler<AsyncResult<ApiDefinition>> resultHandler) {
        if (definition == null) {
            resultHandler.handle(Future.failedFuture("definition is null"));
            return;
        }
        registry.removeIf(d -> d.name().equals(definition.name()));
        registry.add(definition);
        resultHandler.handle(Future.succeededFuture(definition));
    }

    @Override
    public void remove(String name, Handler<AsyncResult<ApiDefinition>> resultHandler) {
        if (name == null) {
            resultHandler.handle(Future.failedFuture("name required"));
            return;
        }
        Optional<ApiDefinition> optional = registry.stream()
                .filter(d -> d.name().equals(name))
                .findFirst();
        if (optional.isPresent()) {
            resultHandler.handle(Future.succeededFuture(optional.get()));
        } else {
            resultHandler.handle(Future.failedFuture("Api: '" + name + "' not found"));
        }
    }

    @Override
    public void getDefinitions(Handler<AsyncResult<List<ApiDefinition>>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(registry));
    }

    @Override
    public void getDefinition(String name, Handler<AsyncResult<ApiDefinition>> resultHandler) {
        if (name == null) {
            resultHandler.handle(Future.failedFuture("name required"));
            return;
        }
        Optional<ApiDefinition> optional = registry.stream()
                .filter(d -> d.name().equals(name))
                .findFirst();
        if (optional.isPresent()) {
            resultHandler.handle(Future.succeededFuture(optional.get()));
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
