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

public class QueryContainsPredicateTest {

    @Test
    public void testNoQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        List<String> queryContains = new ArrayList<>();
        queryContains.add("foo");
        QueryContainsPredicate predicate = new QueryContainsPredicate(queryContains);
        Assert.assertFalse(predicate.test(apiContext));
    }

    @Test
    public void testContainsOneQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        query.put("foo", "bar");
        query.put("start", "1");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        List<String> queryContains = new ArrayList<>();
        queryContains.add("foo");
        QueryContainsPredicate predicate = new QueryContainsPredicate(queryContains);
        Assert.assertTrue(predicate.test(apiContext));
    }

    @Test
    public void testContainsTwoQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        query.put("foo", "bar");
        query.put("start", "1");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        List<String> queryContains = new ArrayList<>();
        queryContains.add("foo");
        queryContains.add("start");
        QueryContainsPredicate predicate = new QueryContainsPredicate(queryContains);
        Assert.assertTrue(predicate.test(apiContext));
    }

    @Test
    public void testNotContainsOneQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        query.put("foo", "bar");
        query.put("start", "1");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        List<String> queryContains = new ArrayList<>();
        queryContains.add(Randoms.randomAlphabet(8));
        QueryContainsPredicate predicate = new QueryContainsPredicate(queryContains);
        Assert.assertFalse(predicate.test(apiContext));
    }

    @Test
    public void testNotContainsTwoQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        query.put("foo", "bar");
        query.put("start", "1");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        List<String> queryContains = new ArrayList<>();
        queryContains.add(Randoms.randomAlphabet(8));
        queryContains.add("start");
        QueryContainsPredicate predicate = new QueryContainsPredicate(queryContains);
        Assert.assertFalse(predicate.test(apiContext));
    }
}
