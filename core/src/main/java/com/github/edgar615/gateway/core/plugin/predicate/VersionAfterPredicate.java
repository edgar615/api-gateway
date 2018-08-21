package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

import java.util.Objects;

public class VersionAfterPredicate implements ApiPredicate {

    private final String version;

    public VersionAfterPredicate(String version) {
        Objects.requireNonNull(version);
        this.version = version;
    }

    public boolean test(ApiContext context) {
        return false;
    }
}
