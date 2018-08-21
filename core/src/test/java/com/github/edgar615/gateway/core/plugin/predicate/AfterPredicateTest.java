package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.collect.ArrayListMultimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AfterPredicateTest {

    @Test
    public void testNotAfter() {
        ApiContext apiContext
                = ApiContext
                .create(HttpMethod.GET, "/", ArrayListMultimap.create(), ArrayListMultimap.create(),
                        null);
        apiContext.addVariable("requestReceivedOn", System.currentTimeMillis() - 60 * 60 * 1000);
        String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        AfterPredicate afterPredicate = new AfterPredicate(now);
        Assert.assertFalse(afterPredicate.test(apiContext));
    }

    @Test
    public void testAfter() {
        ApiContext apiContext
                = ApiContext
                .create(HttpMethod.GET, "/", ArrayListMultimap.create(), ArrayListMultimap.create(),
                        null);
        apiContext.addVariable("requestReceivedOn", System.currentTimeMillis() + 60 * 60 * 1000);
        String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        AfterPredicate afterPredicate = new AfterPredicate(now);
        Assert.assertTrue(afterPredicate.test(apiContext));
    }
}
