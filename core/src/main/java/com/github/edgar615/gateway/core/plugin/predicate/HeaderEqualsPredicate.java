package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

import java.util.*;

public class HeaderEqualsPredicate implements ApiPredicate {

  private final Map<String, String> headers = new HashMap<>();

  public HeaderEqualsPredicate(Map<String, String> headers) {
    Objects.requireNonNull(headers);
    this.headers.putAll(headers);
  }

  public boolean test(ApiContext context) {
    return false;
  }
}
