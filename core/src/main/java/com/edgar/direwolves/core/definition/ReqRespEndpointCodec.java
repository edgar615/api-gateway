package com.edgar.direwolves.core.definition;

import com.google.common.base.Preconditions;

import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/3/8.
 *
 * @author Edgar  Date 2017/3/8
 */
public class ReqRespEndpointCodec implements EndpointCodec {

  @Override
  public Endpoint fromJson(JsonObject jsonObject) {
    String type = jsonObject.getString("type");
    Preconditions.checkNotNull(type, "endpoint type cannot be null");
    Preconditions.checkArgument(type.equalsIgnoreCase("req-resp"),
                                "endpoint name must be req-resp");
    String address = jsonObject.getString("address");
    Preconditions.checkNotNull(address, "endpoint address cannot be null");
    String name = jsonObject.getString("name");
    Preconditions.checkNotNull(name, "endpoint name cannot be null");

    String operation = jsonObject.getString("action");
    return new ReqRespEndpointImpl(name, address, operation);
  }

  @Override
  public JsonObject toJson(Endpoint endpoint) {
    ReqRespEndpoint reqRespEndpoint = (ReqRespEndpoint) endpoint;
    return new JsonObject()
            .put("type", reqRespEndpoint.type())
            .put("name", reqRespEndpoint.name())
            .put("address", reqRespEndpoint.address())
            .put("action", reqRespEndpoint.action());
  }

  @Override
  public String type() {
    return ReqRespEndpoint.TYPE;
  }
}
