package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

import java.util.ArrayList;
import java.util.List;

public class PredicatePlugin implements ApiPlugin {

    private final List<ApiPredicate> predicates = new ArrayList<>();

    @Override
    public String name() {
        return PredicatePlugin.class.getSimpleName();
    }

    public PredicatePlugin add(ApiPredicate predicate) {
        this.predicates.add(predicate);
        return this;
    }

    public PredicatePlugin addAll(List<ApiPredicate> predicates) {
        this.predicates.addAll(predicates);
        return this;
    }

    public List<ApiPredicate> predicates() {
        return predicates;
    }
}
