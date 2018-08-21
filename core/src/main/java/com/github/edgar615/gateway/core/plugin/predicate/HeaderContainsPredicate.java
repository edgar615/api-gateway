package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.utils.MultimapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HeaderContainsPredicate implements ApiPredicate {

    private final List<String> headers = new ArrayList<>();

    public HeaderContainsPredicate(List<String> headers) {
        Objects.requireNonNull(headers);
        this.headers.addAll(headers);
    }

    public boolean test(ApiContext context) {
        for (String header : headers) {
            if (MultimapUtils.getCaseInsensitive(context.headers(), header) == null) {
                return false;
            }
        }
        return true;
    }

    public List<String> headers() {
        return headers;
    }
}
