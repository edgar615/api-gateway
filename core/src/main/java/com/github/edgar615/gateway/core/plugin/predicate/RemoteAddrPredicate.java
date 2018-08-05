package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

import java.util.List;
import java.util.Objects;

public class RemoteAddrPredicate implements ApiPredicate {

  private final List<String> headers;

  public RemoteAddrPredicate(List<String> headers) {
    Objects.requireNonNull(headers);
    this.headers = headers;
  }

  public boolean test(ApiContext context) {
    return false;
  }
}
