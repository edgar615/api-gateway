package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.collect.ArrayListMultimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class BeforePredicateTest {

    @Test
    public void testBefore() {
        ApiContext apiContext
                = ApiContext
                .create(HttpMethod.GET, "/", ArrayListMultimap.create(), ArrayListMultimap.create(),
                        null);
        apiContext.addVariable("requestReceivedOn", System.currentTimeMillis() - 60 * 60 * 1000);
        String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        BeforePredicate beforePredicate = new BeforePredicate(now);
        Assert.assertTrue(beforePredicate.test(apiContext));
    }

    @Test
    public void testNotBefore() {
        ApiContext apiContext
                = ApiContext
                .create(HttpMethod.GET, "/", ArrayListMultimap.create(), ArrayListMultimap.create(),
                        null);
        apiContext.addVariable("requestReceivedOn", System.currentTimeMillis() + 60 * 60 * 1000);
        String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        BeforePredicate beforePredicate = new BeforePredicate(now);
        Assert.assertFalse(beforePredicate.test(apiContext));
    }
}
