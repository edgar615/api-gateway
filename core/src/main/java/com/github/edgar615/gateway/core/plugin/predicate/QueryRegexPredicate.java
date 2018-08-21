package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.utils.MultimapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class QueryRegexPredicate implements ApiPredicate {

    private final Map<String, String> query = new HashMap<>();

    public QueryRegexPredicate(Map<String, String> query) {
        Objects.requireNonNull(query);
        this.query.putAll(query);
    }

    public boolean test(ApiContext context) {
        Set<String> names = query.keySet();
        for (String name : names) {
            String value = query.get(name);
            String queryValue = MultimapUtils.getCaseInsensitive(context.params(), name);
            if (queryValue == null) {
                return false;
            }
            if (!queryValue.matches(value)) {
                return false;
            }
        }
        return true;
    }

    public Map<String, String> query() {
        return query;
    }
}
