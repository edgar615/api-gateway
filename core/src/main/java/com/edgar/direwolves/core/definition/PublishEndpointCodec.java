package com.edgar.direwolves.core.definition;

import com.google.common.base.Preconditions;

import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/3/8.
 *
 * @author Edgar  Date 2017/3/8
 */
public class PublishEndpointCodec implements EndpointCodec {

  @Override
  public Endpoint fromJson(JsonObject jsonObject) {
    String type = jsonObject.getString("type");
    Preconditions.checkNotNull(type, "endpoint type cannot be null");
    Preconditions.checkArgument(type.equalsIgnoreCase("publish"),
                                "endpoint name must be publish");
    String address = jsonObject.getString("address");
    Preconditions.checkNotNull(address, "endpoint address cannot be null");
    String name = jsonObject.getString("name");
    Preconditions.checkNotNull(name, "endpoint name cannot be null");
    return new PublishEndpointImpl(name, address);
  }

  @Override
  public JsonObject toJson(Endpoint endpoint) {
    PublishEndpoint publishEndpoint = (PublishEndpoint) endpoint;
    return new JsonObject()
            .put("type", publishEndpoint.type())
            .put("name", publishEndpoint.name())
            .put("address", publishEndpoint.address());
  }

  @Override
  public String type() {
    return PublishEndpoint.TYPE;
  }
}
