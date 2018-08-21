package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RemoteAddrAppointPredicateTest {

    @Test
    public void testAppoint() {
        List<String> appoint = new ArrayList<>();
        appoint.add("192.168.1.3");
        appoint.add("172.*");
        RemoteAddrAppointPredicate predicate = new RemoteAddrAppointPredicate(appoint);
        Multimap<String, String> header = ArrayListMultimap.create();
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        apiContext.addVariable("request_clientIp", "192.168.1.3");
        Assert.assertTrue(predicate.test(apiContext));

        apiContext.addVariable("request_clientIp", "172.0.0.1");
        Assert.assertTrue(predicate.test(apiContext));

        apiContext.addVariable("request_clientIp", "172.168.135.111");
        Assert.assertTrue(predicate.test(apiContext));

        apiContext.addVariable("request_clientIp", "173.168.135.111");
        Assert.assertFalse(predicate.test(apiContext));

        apiContext.addVariable("request_clientIp", "192.168.1.4");
        Assert.assertFalse(predicate.test(apiContext));
    }
}
