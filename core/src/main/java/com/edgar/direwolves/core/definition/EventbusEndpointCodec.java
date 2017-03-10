package com.edgar.direwolves.core.definition;

import com.google.common.base.Preconditions;

import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/3/8.
 *
 * @author Edgar  Date 2017/3/8
 */
public class EventbusEndpointCodec implements EndpointCodec {

  @Override
  public Endpoint fromJson(JsonObject jsonObject) {
    String type = jsonObject.getString("type");
    Preconditions.checkNotNull(type, "endpoint type cannot be null");
    Preconditions.checkArgument(type.equalsIgnoreCase("eventbus"),
                                "endpoint name must be eventbus");
    String address = jsonObject.getString("address");
    Preconditions.checkNotNull(address, "endpoint address cannot be null");
    String name = jsonObject.getString("name");
    Preconditions.checkNotNull(name, "endpoint name cannot be null");
    String policy = jsonObject.getString("policy");
    Preconditions.checkNotNull(policy, "endpoint policy cannot be null");
    String action = jsonObject.getString("action");
    return new EventbusEndpointImpl(name, address, policy, action);
  }

  @Override
  public JsonObject toJson(Endpoint endpoint) {
    EventbusEndpoint eventbusEndpoint = (EventbusEndpoint) endpoint;
    return new JsonObject()
            .put("type", eventbusEndpoint.type())
            .put("name", eventbusEndpoint.name())
            .put("address", eventbusEndpoint.address())
            .put("policy", eventbusEndpoint.policy())
            .put("action", eventbusEndpoint.action());
  }

  @Override
  public String type() {
    return EventbusEndpoint.TYPE;
  }
}
