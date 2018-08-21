package com.github.edgar615.gateway.core.apidiscovery;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
public interface ApiDefinitionBackend {
    void store(ApiDefinition definition, Handler<AsyncResult<ApiDefinition>> resultHandler);

    void remove(String name, Handler<AsyncResult<ApiDefinition>> resultHandler);

    void getDefinitions(Handler<AsyncResult<List<ApiDefinition>>> resultHandler);

    void getDefinition(String name, Handler<AsyncResult<ApiDefinition>> resultHandler);

    void clear(Handler<AsyncResult<Void>> resultHandler);
}
