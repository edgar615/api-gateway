package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class PredicatePluginTest {

    @Test
    public void testDecode() {
        JsonObject predicate = new JsonObject()
                .put("before", "2018-08-05T17:05:02.717+08:00")
                .put("after", "2018-08-06T17:05:02.717+08:00")
                .put("between", new JsonArray().add("2018-08-05T17:05:02.717+08:00")
                        .add("2018-08-06T17:05:02.717+08:00"));
        JsonObject header = new JsonObject()
                .put("contains", new JsonArray().add("X-Api-Version").add("Content-Type"))
                .put("equals", new JsonObject().put("X-Api-Version", "0.0.1"))
                .put("regex", new JsonObject().put("X-Api-Version", "\\\\d+"));
        predicate.put("header", header);

        JsonObject query = new JsonObject()
                .put("contains", new JsonArray().add("version").add("foo"))
                .put("equals", new JsonObject().put("X-Api-Version", "0.0.1"))
                .put("regex", new JsonObject().put("X-Api-Version", "\\\\d+"));
        predicate.put("query", query);

        JsonObject remoteAddr = new JsonObject()
                .put("appoint", new JsonArray().add("192.168.1.5").add("172.16.*"))
                .put("hash", new JsonObject().put("start", 70).put("end", 80))
                .put("range", new JsonObject().put("start", 3221225731l).put("end", 3232235781l));
        predicate.put("remoteAddr", remoteAddr);

        ApiPlugin plugin =
                new PredicatePluginFactory().decode(new JsonObject().put("predicate", predicate));
        Assert.assertNotNull(plugin);
        PredicatePlugin predicatePlugin = (PredicatePlugin) plugin;
        Assert.assertEquals(12, predicatePlugin.predicates().size());

    }

    @Test
    public void testEncode() {
        PredicatePlugin plugin = new PredicatePlugin();
        ApiPredicate before = new BeforePredicate("2018-08-05T17:05:02.717+08:00");
        plugin.add(before);
        ApiPredicate after = new AfterPredicate("2018-08-06T17:05:02.717+08:00");
        plugin.add(after);
        ApiPredicate between = new BetweenPredicate("2018-08-05T17:05:02.717+08:00",
                                                    "2018-08-06T17:05:02.717+08:00");
        plugin.add(between);
        HeaderContainsPredicate headerContainsPredicate =
                new HeaderContainsPredicate(Lists.newArrayList("X-Api-Version", "Content-Type"));
        plugin.add(headerContainsPredicate);
        HeaderEqualsPredicate headerEqualsPredicate =
                new HeaderEqualsPredicate(ImmutableMap.of("foo", "bar"));
        plugin.add(headerEqualsPredicate);
        HeaderRegexPredicate headerRegexPredicate =
                new HeaderRegexPredicate(ImmutableMap.of("foo", "\\\\d+"));
        plugin.add(headerRegexPredicate);
        QueryContainsPredicate queryContainsPredicate =
                new QueryContainsPredicate(Lists.newArrayList("X-Api-Version", "Content-Type"));
        plugin.add(queryContainsPredicate);
        QueryEqualsPredicate queryEqualsPredicate =
                new QueryEqualsPredicate(ImmutableMap.of("foo", "bar"));
        plugin.add(queryEqualsPredicate);
        QueryRegexPredicate queryRegexPredicate =
                new QueryRegexPredicate(ImmutableMap.of("foo", "\\\\d+"));
        plugin.add(queryRegexPredicate);
        RemoteAddrAppointPredicate addrAppointPredicate =
                new RemoteAddrAppointPredicate(Lists.newArrayList("192.168.0.100"));
        plugin.add(addrAppointPredicate);
        RemoteAddrHashPredicate hashPredicate = new RemoteAddrHashPredicate(50, 60);
        plugin.add(hashPredicate);
        RemoteAddrRangePredicate rangePredicate = new RemoteAddrRangePredicate(100000l, 2000000l);
        plugin.add(rangePredicate);
        JsonObject jsonObject = new PredicatePluginFactory().encode(plugin);
        Assert.assertTrue(jsonObject.getValue("before") instanceof String);
        Assert.assertTrue(jsonObject.getValue("after") instanceof String);
        Assert.assertTrue(jsonObject.getValue("between") instanceof JsonArray);
        Assert.assertTrue(jsonObject.getValue("header") instanceof JsonObject);
        JsonObject header = jsonObject.getJsonObject("header");
        Assert.assertTrue(header.getValue("contains") instanceof JsonArray);
        Assert.assertTrue(header.getValue("equals") instanceof JsonObject);
        Assert.assertTrue(header.getValue("regex") instanceof JsonObject);

        Assert.assertTrue(jsonObject.getValue("query") instanceof JsonObject);
        JsonObject query = jsonObject.getJsonObject("query");
        Assert.assertTrue(query.getValue("contains") instanceof JsonArray);
        Assert.assertTrue(query.getValue("equals") instanceof JsonObject);
        Assert.assertTrue(query.getValue("regex") instanceof JsonObject);

        Assert.assertTrue(jsonObject.getValue("remoteAddr") instanceof JsonObject);
        JsonObject remoteAddr = jsonObject.getJsonObject("remoteAddr");
        Assert.assertTrue(remoteAddr.getValue("appoint") instanceof JsonArray);
        Assert.assertTrue(remoteAddr.getValue("hash") instanceof JsonObject);
        Assert.assertTrue(remoteAddr.getValue("range") instanceof JsonObject);
    }
}
