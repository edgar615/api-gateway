package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.util.base.Randoms;
import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HeaderContainsPredicateTest {

    @Test
    public void testNoHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        List<String> headerContains = new ArrayList<>();
        headerContains.add("x-request-id");
        HeaderContainsPredicate predicate = new HeaderContainsPredicate(headerContains);
        Assert.assertFalse(predicate.test(apiContext));
    }

    @Test
    public void testContainsOneHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        header.put("x-request-id", UUID.randomUUID().toString());
        header.put("Content-Type", "application/json");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        List<String> headerContains = new ArrayList<>();
        headerContains.add("x-request-id");
        HeaderContainsPredicate predicate = new HeaderContainsPredicate(headerContains);
        Assert.assertTrue(predicate.test(apiContext));
    }

    @Test
    public void testContainsTwoHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        header.put("x-request-id", UUID.randomUUID().toString());
        header.put("Content-Type", "application/json");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        List<String> headerContains = new ArrayList<>();
        headerContains.add("x-request-id");
        headerContains.add("Content-Type");
        HeaderContainsPredicate predicate = new HeaderContainsPredicate(headerContains);
        Assert.assertTrue(predicate.test(apiContext));
    }

    @Test
    public void testNotContainsOneHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        header.put("x-request-id", UUID.randomUUID().toString());
        header.put("Content-Type", "application/json");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        List<String> headerContains = new ArrayList<>();
        headerContains.add(Randoms.randomAlphabet(8));
        HeaderContainsPredicate predicate = new HeaderContainsPredicate(headerContains);
        Assert.assertFalse(predicate.test(apiContext));
    }

    @Test
    public void testNotContainsTwoHeader() {
        Multimap<String, String> header = ArrayListMultimap.create();
        header.put("x-request-id", UUID.randomUUID().toString());
        header.put("Content-Type", "application/json");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        List<String> headerContains = new ArrayList<>();
        headerContains.add("x-request-id");
        headerContains.add(Randoms.randomAlphabet(8));
        HeaderContainsPredicate predicate = new HeaderContainsPredicate(headerContains);
        Assert.assertFalse(predicate.test(apiContext));
    }
}
