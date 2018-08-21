package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.base.Preconditions;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

public class WeightPredicate implements ApiPredicate {

    private final int weight;

    public WeightPredicate(int weight) {
        Preconditions.checkArgument(weight >= 0, "weight must great than 0");
        Preconditions.checkArgument(weight <= 100, "weight must less than 100");
        this.weight = weight;
    }

    public boolean test(ApiContext context) {
        return false;
    }
}
