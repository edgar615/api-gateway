package com.github.edgar615.gateway.core.plugin.predicate;

import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeTest {

    @Test
    public void test() {
        long epoch = System.currentTimeMillis();
        ZonedDateTime now = Instant.ofEpochMilli(epoch).atOffset(ZoneOffset.ofTotalSeconds(0))
                .toZonedDateTime();
//    2018-08-05T08:57:56Z
        System.out.println(now);
        ZonedDateTime now2 = Instant.ofEpochMilli(epoch).atOffset(ZoneOffset.ofHours(8))
                .toZonedDateTime();
        System.out.println(now2);
        ZonedDateTime beforeDateTime = ZonedDateTime
                .parse("2018-08-05T17:05:02.717+08:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        System.out.println(beforeDateTime.isBefore(now));
    }
}
