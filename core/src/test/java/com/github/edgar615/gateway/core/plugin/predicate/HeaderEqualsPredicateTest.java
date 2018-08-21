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

public class HeaderEqualsPredicateTest {

    @Test
    public void testNoHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        Map<String, String> headerEquals = new HashMap<>();
        headerEquals.put("x-request-id", "123456");
        HeaderEqualsPredicate predicate = new HeaderEqualsPredicate(headerEquals);
        Assert.assertFalse(predicate.test(apiContext));
    }

    @Test
    public void testEqualsOneHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        header.put("x-request-id", "123456");
        header.put("Content-Type", "application/json");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        Map<String, String> headerEquals = new HashMap<>();
        headerEquals.put("x-request-id", "123456");
        HeaderEqualsPredicate predicate = new HeaderEqualsPredicate(headerEquals);
        Assert.assertTrue(predicate.test(apiContext));
    }

    @Test
    public void testEqualsTwoHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        header.put("x-request-id", "123456");
        header.put("Content-Type", "application/json");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        Map<String, String> headerEquals = new HashMap<>();
        headerEquals.put("x-request-id", "123456");
        headerEquals.put("Content-Type", "application/json");
        HeaderEqualsPredicate predicate = new HeaderEqualsPredicate(headerEquals);
        Assert.assertTrue(predicate.test(apiContext));
    }

    @Test
    public void testNotEqualsOneHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        header.put("x-request-id", "123456");
        header.put("Content-Type", "application/json");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        Map<String, String> headerEquals = new HashMap<>();
        headerEquals.put("x-request-id", UUID.randomUUID().toString());
        HeaderEqualsPredicate predicate = new HeaderEqualsPredicate(headerEquals);
        Assert.assertFalse(predicate.test(apiContext));
    }

    @Test
    public void testNotEqualsTwoHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        header.put("x-request-id", "123456");
        header.put("Content-Type", "application/json");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        Map<String, String> headerEquals = new HashMap<>();
        headerEquals.put("x-request-id", UUID.randomUUID().toString());
        headerEquals.put("Content-Type", "text/html");
        HeaderEqualsPredicate predicate = new HeaderEqualsPredicate(headerEquals);
        Assert.assertFalse(predicate.test(apiContext));
    }
}
