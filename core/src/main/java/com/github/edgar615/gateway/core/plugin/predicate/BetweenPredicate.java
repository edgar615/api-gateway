package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class BetweenPredicate implements ApiPredicate {

    private final ZonedDateTime zoneStartDateTime;

    private final ZonedDateTime zoneEndDateTime;

    private final String startDateTime;

    private final String endDateTime;

    public BetweenPredicate(String startDateTime, String endDateTime) {
        Objects.requireNonNull(startDateTime);
        Objects.requireNonNull(endDateTime);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.zoneStartDateTime =
                ZonedDateTime.parse(startDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.zoneEndDateTime =
                ZonedDateTime.parse(endDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        ;
    }

    public boolean test(ApiContext context) {
        long requestReceivedOn = (long) context.variables().getOrDefault("requestReceivedOn",
                                                                         System.currentTimeMillis
                                                                                 ());
        ZonedDateTime now =
                Instant.ofEpochMilli(requestReceivedOn).atOffset(ZoneOffset.ofTotalSeconds(0))
                        .toZonedDateTime();
        return now.isBefore(zoneEndDateTime) && now.isAfter(zoneStartDateTime);
    }

    public String startDateTime() {
        return startDateTime;
    }

    public String endDateTime() {
        return endDateTime;
    }
}
