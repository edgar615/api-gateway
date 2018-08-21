package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class QueryRegexPredicateTest {

    @Test
    public void testNoQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        Map<String, String> queryRegex = new HashMap<>();
        queryRegex.put("start", "[0-9]+");
        QueryRegexPredicate predicate = new QueryRegexPredicate(queryRegex);
        Assert.assertFalse(predicate.test(apiContext));
    }

    @Test
    public void testRegexOneQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        query.put("foo", "bar");
        query.put("start", "1");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        Map<String, String> queryRegex = new HashMap<>();
        queryRegex.put("start", "[0-9]+");
        QueryRegexPredicate predicate = new QueryRegexPredicate(queryRegex);
        Assert.assertTrue(predicate.test(apiContext));
    }

    @Test
    public void testNotRegexOneQuery() {
        Multimap<String, String> query = ArrayListMultimap.create();
        query.put("foo", "bar");
        query.put("start", "ab");
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", ArrayListMultimap.create(), query, null);
        Map<String, String> queryRegex = new HashMap<>();
        queryRegex.put("start", "[0-9]+");
        QueryRegexPredicate predicate = new QueryRegexPredicate(queryRegex);
        Assert.assertFalse(predicate.test(apiContext));
    }

}
