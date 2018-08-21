package com.github.edgar615.gateway.core.plugin.order;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

public class OrderPlugin implements ApiPlugin {

    private int order;

    public OrderPlugin(int order) {
        this.order = order;
    }

    public int order() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String name() {
        return OrderPlugin.class.getSimpleName();
    }
}
