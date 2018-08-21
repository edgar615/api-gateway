package com.github.edgar615.gateway.core.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;

/**
 * Created by Edgar on 2017/3/8.
 *
 * @author Edgar  Date 2017/3/8
 */
class EventbusEndpointImpl implements EventbusEndpoint {
    /**
     * endpoint名称
     */
    private final String name;

    /**
     * 事件地址
     */
    private final String address;

    /**
     * 请求头.
     */
    private final Multimap<String, String> headers;

    /**
     * 三种策略：pub-sub、point-point、req-resp
     */
    private final String policy;

    /**
     * 消息的活动，用于对一个消息做进一步细分
     */
    private final String action;

    EventbusEndpointImpl(String name, String address, String policy, String action,
                         Multimap<String, String> headers) {
        Preconditions.checkNotNull(name, "name can not be null");
        Preconditions.checkNotNull(address, "address can not be null");
        Preconditions.checkNotNull(policy, "policy can not be null");
        if (!"pub-sub".equalsIgnoreCase(policy)
            && !"point-point".equalsIgnoreCase(policy)
            && !"req-resp".equalsIgnoreCase(policy)) {
            throw SystemException.create(DefaultErrorCode.INVALID_ARGS)
                    .set("details", "policy must be pub-sub | point-point | req-resp");
        }
        this.name = name;
        this.address = address;
        this.policy = policy;
        this.action = action;
        if (headers == null) {
            this.headers = ArrayListMultimap.create();
        } else {
            this.headers = ArrayListMultimap.create(headers);
        }
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public Multimap<String, String> headers() {
        return headers;
    }

    @Override
    public String policy() {
        return policy;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("EventbusEndpoint")
                .add("name", name)
                .add("address", address)
                .add("policy", policy)
                .add("headers", headers)
                .add("action", action)
                .toString();
    }
}
