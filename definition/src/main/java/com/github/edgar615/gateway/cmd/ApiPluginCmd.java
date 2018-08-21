package com.github.edgar615.gateway.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.gateway.core.cmd.ApiCmd;
import com.github.edgar615.gateway.core.cmd.ApiSubCmd;
import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * 根据名称获取某个API定义，
 * 参数: api的JSON配置文件.
 * 命令字: api.get
 * 参数 {name : 要删除的API名称}
 *
 * @author Edgar  Date 2017/1/19
 */
class ApiPluginCmd implements ApiCmd {

    private final Vertx vertx;

    private final Multimap<String, Rule> rules = ArrayListMultimap.create();

    private final List<ApiSubCmd> subCmdList
            = Lists.newArrayList(ServiceLoader.load(ApiSubCmd.class));

    private final JsonObject configuration = new JsonObject();

    ApiPluginCmd(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        rules.put("namespace", Rule.required());
        rules.put("name", Rule.required());
        rules.put("subcmd", Rule.required());
        setConfig(config, configuration);
    }

    @Override
    public String cmd() {
        return "api.plugin";
    }

    @Override
    public Future<JsonObject> doHandle(JsonObject jsonObject) {
        Validations.validate(jsonObject.getMap(), rules);
        String namespace = jsonObject.getString("namespace");
        String name = jsonObject.getString("name", "*");
        JsonObject filter = new JsonObject();
        if (name != null) {
            filter.put("name", name);
        }
        Future<JsonObject> future = Future.future();
        String subCmd = jsonObject.getString("subcmd");
        jsonObject.remove("name");
        jsonObject.remove("namespace");
        jsonObject.remove("subcmd");
        List<ApiSubCmd> filterCmdList = subCmdList.stream()
                .filter(s -> subCmd.equalsIgnoreCase(s.cmd()))
                .collect(Collectors.toList());
        if (filterCmdList.isEmpty()) {
            future.fail(SystemException.create(DefaultErrorCode.INVALID_ARGS)
                                .set("details", String.format("subcmd:%s undefined", subCmd)));
            return future;
        }

        ApiDiscovery discovery =
                ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName(namespace));
        discovery.getDefinitions(filter, ar -> {
            if (ar.failed()) {
                future.fail(ar.cause());
                return;
            }
            List<ApiDefinition> definitions = ar.result();
            if (definitions.isEmpty()) {
                future.complete(succeedResult());
            } else {
                doSubCmd(discovery, definitions, filterCmdList.get(0), jsonObject.copy(), future);
            }
        });
        return future;
    }

    private void doSubCmd(ApiDiscovery discovery, List<ApiDefinition> definitions,
                          ApiSubCmd subCmd,
                          JsonObject jsonObject, Future<JsonObject> complete) {
        List<Future> futures = new ArrayList<>();
        for (ApiDefinition definition : definitions) {
            Future<Void> future = Future.future();
            futures.add(future);
            subCmd.handle(definition, jsonObject);
            discovery.publish(definition, ar -> {
                if (ar.succeeded()) {
                    future.complete();
                } else {
                    future.fail(ar.cause());
                }
            });
        }
        CompositeFuture.all(futures)
                .setHandler(ar -> {
                    if (ar.succeeded()) {
                        complete.complete(succeedResult());
                    } else {
                        complete.fail(ar.cause());
                    }
                });
    }

}
