package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.utils.MultimapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QueryContainsPredicate implements ApiPredicate {

    private final List<String> query = new ArrayList<>();

    public QueryContainsPredicate(List<String> query) {
        Objects.requireNonNull(query);
        this.query.addAll(query);
    }

    public boolean test(ApiContext context) {
        for (String name : query) {
            if (MultimapUtils.getCaseInsensitive(context.params(), name) == null) {
                return false;
            }
        }
        return true;
    }

    public List<String> query() {
        return query;
    }
}
