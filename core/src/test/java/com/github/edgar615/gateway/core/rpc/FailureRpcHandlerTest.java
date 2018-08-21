package com.github.edgar615.gateway.core.rpc;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2017/1/6.
 *
 * @author Edgar  Date 2017/1/6
 */
public class FailureRpcHandlerTest {

    @Test
    public void alwaysReturnFailed() {
        RpcHandler handler = FailureRpcHandler.create("failed");
        Assert.assertEquals("failed", handler.type());
//    handler.handle(HttpRpcRequest.create(UUID.randomUUID().toString(),
//                                         UUID.randomUUID().toString()))
//    .setHandler(ar -> {
//      Assert.assertTrue(ar.failed());
//      Assert.assertFalse(ar.succeeded());
//    });

    }
}
