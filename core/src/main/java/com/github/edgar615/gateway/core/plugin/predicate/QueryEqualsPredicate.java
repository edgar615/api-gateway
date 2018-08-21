package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.utils.MultimapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class QueryEqualsPredicate implements ApiPredicate {

    private final Map<String, String> query = new HashMap<>();

    public QueryEqualsPredicate(Map<String, String> query) {
        Objects.requireNonNull(query);
        this.query.putAll(query);
    }

    public boolean test(ApiContext context) {
        Set<String> names = query.keySet();
        for (String name : names) {
            String value = query.get(name);
            String paramValue = MultimapUtils.getCaseInsensitive(context.params(), name);
            if (paramValue == null) {
                return false;
            }
            if (!value.equals(paramValue)) {
                return false;
            }
        }
        return true;
    }

    public Map<String, String> query() {
        return query;
    }
}
