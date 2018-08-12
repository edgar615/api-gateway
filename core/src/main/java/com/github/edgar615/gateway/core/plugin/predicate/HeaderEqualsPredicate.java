package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.utils.MultimapUtils;

import java.util.*;

public class HeaderEqualsPredicate implements ApiPredicate {

  private final Map<String, String> headers = new HashMap<>();

  public HeaderEqualsPredicate(Map<String, String> headers) {
    Objects.requireNonNull(headers);
    this.headers.putAll(headers);
  }

  public boolean test(ApiContext context) {
    Set<String> names = headers.keySet();
    for (String name : names) {
      String value = headers.get(name);
      String headerValue = MultimapUtils.getCaseInsensitive(context.headers(), name);
      if (headerValue == null) {
        return false;
      }
      if (!value.equals(headerValue)) {
        return false;
      }
    }
    return true;
  }

  public Map<String, String> headers() {
    return headers;
  }
}
