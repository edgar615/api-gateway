package com.github.edgar615.gateway.test.apifinder;

import com.github.edgar615.util.exception.DefaultErrorCode;
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
public class ConflictTest {

//  @Test
//  public void test() {
//    System.out.println(Randoms.randomLowerAlphabet(20));
//    System.out.println(UUID.randomUUID().toString().replace("-", ""));
//  }

    @Test
    public void testOk(TestContext testContext) {
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost", "/conflict")
                .handler(resp -> {
                    testContext.assertEquals(500, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        testContext.assertEquals(DefaultErrorCode.CONFLICT.getNumber(),
                                                 body.toJsonObject().getInteger("code"));
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }


}
