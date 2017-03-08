package com.edgar.direwolves.core.definition;

import com.google.common.base.Preconditions;

import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/3/8.
 *
 * @author Edgar  Date 2017/3/8
 */
public class PointToPointEndpointCodec implements EndpointCodec {

  @Override
  public Endpoint fromJson(JsonObject jsonObject) {
    String type = jsonObject.getString("type");
    Preconditions.checkNotNull(type, "endpoint type cannot be null");
    Preconditions.checkArgument(type.equalsIgnoreCase("point"),
                                "endpoint name must be point");
    String address = jsonObject.getString("address");
    Preconditions.checkNotNull(address, "endpoint address cannot be null");
    String name = jsonObject.getString("name");
    Preconditions.checkNotNull(name, "endpoint name cannot be null");
    return new PointToPointEndpointImpl(name, address);
  }

  @Override
  public JsonObject toJson(Endpoint endpoint) {
    PointToPointEndpoint pointToPointEndpoint = (PointToPointEndpoint) endpoint;
    return new JsonObject()
            .put("type", pointToPointEndpoint.type())
            .put("name", pointToPointEndpoint.name())
            .put("address", pointToPointEndpoint.address());
  }

  @Override
  public String type() {
    return PointToPointEndpoint.TYPE;
  }
}
