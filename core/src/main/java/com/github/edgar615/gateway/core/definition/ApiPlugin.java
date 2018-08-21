package com.github.edgar615.gateway.core.definition;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public interface ApiPlugin {
    List<ApiPluginFactory> factories = Lists.newArrayList(
            ServiceLoader.load(ApiPluginFactory.class));

    /**
     * @return 插件名称
     */
    String name();

    static ApiPlugin create(String name) {
        return factory(name).create();
    }

    static ApiPluginFactory factory(String name) {
        Preconditions.checkNotNull(name, "name cannot null");
        Optional<ApiPluginFactory> optional
                = factories.stream().filter(f -> name.equalsIgnoreCase(f.name()))
                .findAny();
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new NoSuchElementException("no such factory->" + name);
    }

    default JsonObject encode() {
        return factories.stream().filter(f -> this.name().equalsIgnoreCase(f.name()))
                .map(f -> f.encode(this))
                .findFirst().orElseGet(() -> new JsonObject());
    }

}
