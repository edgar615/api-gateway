package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class VersionBeforePredicate implements ApiPredicate {

  private final String version;

  public VersionBeforePredicate(String version) {
    Objects.requireNonNull(version);
    this.version = version;
  }

  public boolean test(ApiContext context) {
    return false;
  }
}
