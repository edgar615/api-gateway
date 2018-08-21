package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeaderRegexPredicateTest {


    @Test
    public void testNoHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        Map<String, String> headerRegex = new HashMap<>();
        headerRegex.put("x-request-id", "[0-9]{6}");
        HeaderRegexPredicate predicate = new HeaderRegexPredicate(headerRegex);
        Assert.assertFalse(predicate.test(apiContext));
    }

    @Test
    public void testRegexOneHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        header.put("x-request-id", "123456");
        header.put("Content-Type", "application/json");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        Map<String, String> headerRegex = new HashMap<>();
        headerRegex.put("x-request-id", "[0-9]{6}");
        HeaderRegexPredicate predicate = new HeaderRegexPredicate(headerRegex);
        Assert.assertTrue(predicate.test(apiContext));
    }

    @Test
    public void testNotRegexOneHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        header.put("x-request-id", UUID.randomUUID().toString());
        header.put("Content-Type", "application/json");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        Map<String, String> headerRegex = new HashMap<>();
        headerRegex.put("x-request-id", "[0-9]{6}");
        HeaderRegexPredicate predicate = new HeaderRegexPredicate(headerRegex);
        Assert.assertFalse(predicate.test(apiContext));
    }
}
