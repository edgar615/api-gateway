package com.github.edgar615.gateway.benchmark.apidiscovery;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.verticle.FileApiDiscoveryVerticle;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ApiBackend {
    private Vertx vertx;

    private ApiDiscovery apiDiscovery;

    public ApiBackend() {
        vertx = Vertx.vertx();
        apiDiscovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions());
        JsonObject config = new JsonObject()
                .put("path", "E:\\iotp\\iotp-app\\router\\api");
        vertx.deployVerticle(FileApiDiscoveryVerticle.class,
                new DeploymentOptions().setConfig(config),
                Future.future());
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getDefinitions(JsonObject jsonObject,
                               Handler<AsyncResult<List<ApiDefinition>>>
                                       handler) {
        apiDiscovery.getDefinitions(jsonObject, handler);
    }

    public void close() {
        vertx.close();
    }
}