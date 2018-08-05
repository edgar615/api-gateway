package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class QueryEqualsPredicate implements ApiPredicate {

  private final Map<String, String> query = new HashMap<>();

  public QueryEqualsPredicate(Map<String, String> query) {
    Objects.requireNonNull(query);
    this.query.putAll(query);
  }

  public boolean test(ApiContext context) {
    return false;
  }
}
