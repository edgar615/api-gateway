package com.github.edgar615.direwolves.core.definition;

import com.google.common.base.Preconditions;

import io.vertx.core.json.JsonObject;

/**
 * DummyEndpoint的解码器.
 *
 * @author Edgar  Date 2017/3/8
 */
public class DummyEndpointCodec implements EndpointCodec {

  @Override
  public Endpoint fromJson(JsonObject jsonObject) {
    String type = jsonObject.getString("type");
    Preconditions.checkNotNull(type, "endpoint type cannot be null");
    Preconditions.checkArgument(type.equalsIgnoreCase("dummy"),
        "endpoint name must be dummy");
    String name = jsonObject.getString("name");
    Preconditions.checkNotNull(name, "endpoint name cannot be null");
    JsonObject result = jsonObject.getJsonObject("result", new JsonObject());

    return new DummyEndpointImpl(name, result);
  }

  @Override
  public JsonObject toJson(Endpoint endpoint) {
    DummyEndpoint dummyEndpoint = (DummyEndpoint) endpoint;
    return new JsonObject()
        .put("type", dummyEndpoint.type())
        .put("name", dummyEndpoint.name())
        .put("result", dummyEndpoint.result());
  }

  @Override
  public String type() {
    return DummyEndpoint.TYPE;
  }
}
