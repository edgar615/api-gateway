package com.github.edgar615.gateway.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

public class BeforePredicate implements ApiPredicate {
    @Override
    public boolean test(ApiContext apiContext) {
        return false;
    }
}
