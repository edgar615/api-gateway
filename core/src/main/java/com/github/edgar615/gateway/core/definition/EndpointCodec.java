package com.github.edgar615.gateway.core.definition;

import io.vertx.core.json.JsonObject;

/**
 * Endpoint的编解码类.
 *
 * @author Edgar  Date 2016/9/12
 */
public interface EndpointCodec {

    Endpoint fromJson(JsonObject jsonObject);

    JsonObject toJson(Endpoint endpoint);

    String type();
}
