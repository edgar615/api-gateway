package com.github.edgar615.gateway.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

public class HeaderPredicate implements ApiPredicate {
    @Override
    public boolean test(ApiContext apiContext) {
//    apiContext.apiDefinition().regexStyle()
//    apiContext.headers().containsKey()
        return false;
    }
}
