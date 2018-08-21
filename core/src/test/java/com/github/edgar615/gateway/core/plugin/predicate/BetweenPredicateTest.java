package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.collect.ArrayListMultimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class BetweenPredicateTest {

    @Test
    public void testBefore() {
        ApiContext apiContext
                = ApiContext
                .create(HttpMethod.GET, "/", ArrayListMultimap.create(), ArrayListMultimap.create(),
                        null);
        apiContext.addVariable("requestReceivedOn", System.currentTimeMillis() - 60 * 60 * 1000);
        String start =
                ZonedDateTime.now().minusSeconds(60).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String end =
                ZonedDateTime.now().plusSeconds(60).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        BetweenPredicate betweenPredicate = new BetweenPredicate(start, end);
        Assert.assertFalse(betweenPredicate.test(apiContext));
    }

    @Test
    public void testAfter() {
        ApiContext apiContext
                = ApiContext
                .create(HttpMethod.GET, "/", ArrayListMultimap.create(), ArrayListMultimap.create(),
                        null);
        apiContext.addVariable("requestReceivedOn", System.currentTimeMillis());
        String start =
                ZonedDateTime.now().plusSeconds(60).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String end =
                ZonedDateTime.now().plusSeconds(100).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        BetweenPredicate betweenPredicate = new BetweenPredicate(start, end);
        Assert.assertFalse(betweenPredicate.test(apiContext));
    }

    @Test
    public void testBetween() {
        ApiContext apiContext
                = ApiContext
                .create(HttpMethod.GET, "/", ArrayListMultimap.create(), ArrayListMultimap.create(),
                        null);
        apiContext.addVariable("requestReceivedOn", System.currentTimeMillis());
        String start =
                ZonedDateTime.now().minusSeconds(60).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String end =
                ZonedDateTime.now().plusSeconds(100).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        BetweenPredicate betweenPredicate = new BetweenPredicate(start, end);
        Assert.assertTrue(betweenPredicate.test(apiContext));
    }
}
