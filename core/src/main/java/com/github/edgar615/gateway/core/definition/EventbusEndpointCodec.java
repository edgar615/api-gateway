package com.github.edgar615.gateway.core.definition;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

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
        String name = jsonObject.getString("name", "default");
        Preconditions.checkNotNull(name, "endpoint name cannot be null");
        String policy = jsonObject.getString("policy");
        Preconditions.checkNotNull(policy, "endpoint policy cannot be null");
        JsonObject header = jsonObject.getJsonObject("header", new JsonObject());
        Multimap<String, String> headers = ArrayListMultimap.create();
        for (String key : header.fieldNames()) {
            Object value = header.getValue(key);
            if (value instanceof JsonArray) {
                JsonArray jsonArray = (JsonArray) value;
                for (int i = 0; i < jsonArray.size(); i++) {
                    headers.put(key, jsonArray.getValue(i).toString());
                }
            } else {
                headers.put(key, value.toString());
            }
        }

        return new EventbusEndpointImpl(name, address, policy, jsonObject.getString("action"),
                                        headers);
    }

    @Override
    public JsonObject toJson(Endpoint endpoint) {
        EventbusEndpoint eventbusEndpoint = (EventbusEndpoint) endpoint;
        JsonObject header = new JsonObject();
        for (String key : eventbusEndpoint.headers().keySet()) {
            List<String> values = new ArrayList<>(eventbusEndpoint.headers().get(key));
            header.put(key, values);
        }
        JsonObject jsonObject = new JsonObject()
                .put("type", eventbusEndpoint.type())
                .put("name", eventbusEndpoint.name())
                .put("address", eventbusEndpoint.address())
                .put("policy", eventbusEndpoint.policy())
                .put("header", header);
        if (!Strings.isNullOrEmpty(((EventbusEndpoint) endpoint).action())) {
            jsonObject.put("action", ((EventbusEndpoint) endpoint).action());
        }
        return jsonObject;
    }

    @Override
    public String type() {
        return EventbusEndpoint.TYPE;
    }
}
