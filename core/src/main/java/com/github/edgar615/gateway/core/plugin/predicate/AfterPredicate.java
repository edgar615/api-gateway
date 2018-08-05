package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class AfterPredicate implements ApiPredicate {

  private final ZonedDateTime datetime;

  public AfterPredicate(String datetime) {
    Objects.requireNonNull(datetime);
    this.datetime = ZonedDateTime.parse(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }

  public boolean test(ApiContext context) {
    long requestReceivedOn = (long) context.variables().getOrDefault("requestReceivedOn",
            System.currentTimeMillis());
    ZonedDateTime now = Instant.ofEpochMilli(requestReceivedOn).atOffset(ZoneOffset.ofTotalSeconds(0))
            .toZonedDateTime();
    return now.isAfter(datetime);
  }
}
