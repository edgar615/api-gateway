package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class QueryEqualsPredicateTest {

    @Test
    public void testNoQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        Map<String, String> queryEquals = new HashMap<>();
        queryEquals.put("foo", "bar");
        QueryEqualsPredicate predicate = new QueryEqualsPredicate(queryEquals);
        Assert.assertFalse(predicate.test(apiContext));
    }

    @Test
    public void testEqualsOneQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        query.put("foo", "bar");
        query.put("start", "1");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        Map<String, String> queryEquals = new HashMap<>();
        queryEquals.put("foo", "bar");
        QueryEqualsPredicate predicate = new QueryEqualsPredicate(queryEquals);
        Assert.assertTrue(predicate.test(apiContext));
    }

    @Test
    public void testEqualsTwoQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        query.put("foo", "bar");
        query.put("start", "1");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        Map<String, String> queryEquals = new HashMap<>();
        queryEquals.put("foo", "bar");
        queryEquals.put("start", "1");
        QueryEqualsPredicate predicate = new QueryEqualsPredicate(queryEquals);
        Assert.assertTrue(predicate.test(apiContext));
    }

    @Test
    public void testNotEqualsOneQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        query.put("foo", "bar");
        query.put("start", "1");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        Map<String, String> queryEquals = new HashMap<>();
        queryEquals.put("foo", "xxx");
        QueryEqualsPredicate predicate = new QueryEqualsPredicate(queryEquals);
        Assert.assertFalse(predicate.test(apiContext));
    }

    @Test
    public void testNotEqualsTwoQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        query.put("foo", "bar");
        query.put("start", "1");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        Map<String, String> queryEquals = new HashMap<>();
        queryEquals.put("foo", "xxx");
        queryEquals.put("start", "5");
        QueryEqualsPredicate predicate = new QueryEqualsPredicate(queryEquals);
        Assert.assertFalse(predicate.test(apiContext));
    }
}
