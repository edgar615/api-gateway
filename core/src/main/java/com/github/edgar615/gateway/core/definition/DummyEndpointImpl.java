package com.github.edgar615.gateway.core.definition;

import io.vertx.core.json.JsonObject;

/**
 * 不做远程调用的endpoint，它会直接返回一个JSON对象.
 *
 * @author Edgar  Date 2017/3/8
 */
class DummyEndpointImpl implements DummyEndpoint {

    /**
     * endpoint名称
     */
    private final String name;

    private final JsonObject result;

    DummyEndpointImpl(String name, JsonObject result) {
        this.name = name;
        if (result == null) {
            this.result = new JsonObject();
        } else {
            this.result = result;
        }
    }


    @Override
    public JsonObject result() {
        return result.copy();
    }

    @Override
    public String name() {
        return name;
    }
}
