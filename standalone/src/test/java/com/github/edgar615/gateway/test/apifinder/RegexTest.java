package com.github.edgar615.gateway.test.apifinder;

import com.github.edgar615.util.base.Randoms;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
@RunWith(VertxUnitRunner.class)
public class RegexTest {

    @Test
    public void testOk(TestContext testContext) {
        AtomicBoolean check = new AtomicBoolean();
        String param0 = Randoms.randomNumber(5);
        String param1 = Randoms.randomAlphabetAndNum(10);
        String url = "/regex/" + param0 + "/test/" + param1;
        Vertx.vertx().createHttpClient().get(9000, "localhost", url)
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        testContext.assertEquals(param0,
                                                 body.toJsonObject().getString("param0"));
                        testContext.assertEquals(param1,
                                                 body.toJsonObject().getString("param1"));
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }


}
