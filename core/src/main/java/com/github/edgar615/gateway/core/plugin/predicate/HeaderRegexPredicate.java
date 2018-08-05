package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HeaderRegexPredicate implements ApiPredicate {

  private final Map<String, String> headers = new HashMap<>();

  public HeaderRegexPredicate(Map<String, String> headers) {
    Objects.requireNonNull(headers);
    this.headers.putAll(headers);
  }

  public boolean test(ApiContext context) {
    return false;
  }
}
