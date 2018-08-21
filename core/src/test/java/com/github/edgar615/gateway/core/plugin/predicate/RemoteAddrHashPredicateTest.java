package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.util.net.IPUtils;
import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

public class RemoteAddrHashPredicateTest {

    @Test
    public void testHash() {
        System.out.println(IPUtils.ipToLong("192.168.1.3") % 100);
        System.out.println(IPUtils.ipToLong("172.168.135.111") % 100);
        System.out.println(IPUtils.ipToLong("192.168.1.5") % 100);
        System.out.println(IPUtils.ipToLong("169.168.1.5") % 100);
        RemoteAddrHashPredicate predicate = new RemoteAddrHashPredicate(60, 80);
        Multimap<String, String> header = ArrayListMultimap.create();
        ApiContext apiContext
                = ApiContext.create(HttpMethod.GET, "/", header, ArrayListMultimap.create(), null);
        apiContext.addVariable("request_clientIp", "192.168.1.3");
        Assert.assertTrue(predicate.test(apiContext));

        apiContext.addVariable("request_clientIp", "172.168.135.111");
        Assert.assertTrue(predicate.test(apiContext));

        apiContext.addVariable("request_clientIp", "192.168.1.5");
        Assert.assertFalse(predicate.test(apiContext));

        apiContext.addVariable("request_clientIp", "169.168.1.5");
        Assert.assertFalse(predicate.test(apiContext));
    }
}
