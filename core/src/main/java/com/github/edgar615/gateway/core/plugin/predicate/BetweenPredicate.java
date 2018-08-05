package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class BetweenPredicate implements ApiPredicate {

  private final ZonedDateTime startDateTime;

  private final ZonedDateTime endDateTime;

  public BetweenPredicate(String startDateTime, String endDateTime) {
    Objects.requireNonNull(startDateTime);
    Objects.requireNonNull(endDateTime);
    this.startDateTime = ZonedDateTime.parse(startDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    this.endDateTime =  ZonedDateTime.parse(endDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);;
  }

  public boolean test(ApiContext context) {
    long requestReceivedOn = (long) context.variables().getOrDefault("requestReceivedOn",
            System.currentTimeMillis());
    ZonedDateTime now = Instant.ofEpochMilli(requestReceivedOn).atOffset(ZoneOffset.ofTotalSeconds(0))
            .toZonedDateTime();
    return now.isBefore(endDateTime) && now.isAfter(startDateTime);
  }
}
