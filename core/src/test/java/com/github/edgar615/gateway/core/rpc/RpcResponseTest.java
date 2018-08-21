package com.github.edgar615.gateway.core.rpc;

import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/9/21.
 *
 * @author Edgar  Date 2016/9/21
 */
public class RpcResponseTest {

    @Test
    public void testEmptyJsonObject() {
        JsonObject jsonObject = new JsonObject();
        Buffer buffer = Buffer.buffer();
        jsonObject.writeToBuffer(buffer);
        RpcResponse rpcResponse = RpcResponse.create("test", 200, buffer, 0);
        Assert.assertFalse(rpcResponse.isArray());
        Assert.assertNotNull(rpcResponse.responseObject());
        Assert.assertNull(rpcResponse.responseArray());
        Assert.assertEquals("{}", rpcResponse.responseObject().encode());
    }

    @Test
    public void testJsonObject() {
        JsonObject jsonObject = new JsonObject().put("foo", "bar");
        Buffer buffer = Buffer.buffer();
        jsonObject.writeToBuffer(buffer);
        RpcResponse rpcResponse = RpcResponse.create("test", 200, buffer, 0);
        Assert.assertFalse(rpcResponse.isArray());
        Assert.assertNotNull(rpcResponse.responseObject());
        Assert.assertNull(rpcResponse.responseArray());
        Assert.assertEquals(jsonObject.encode(), rpcResponse.responseObject().encode());
    }

    @Test
    public void testEmptyJsonArray() {
        JsonArray jsonArray = new JsonArray();
        Buffer buffer = Buffer.buffer();
        jsonArray.writeToBuffer(buffer);
        RpcResponse rpcResponse = RpcResponse.create("test", 200, buffer, 0);
        Assert.assertTrue(rpcResponse.isArray());
        Assert.assertNull(rpcResponse.responseObject());
        Assert.assertNotNull(rpcResponse.responseArray());
        Assert.assertEquals("[]", rpcResponse.responseArray().encode());
    }

    @Test
    public void testJsonArray() {
        JsonArray jsonArray = new JsonArray()
                .add(new JsonObject().put("foo", "bar"));
        Buffer buffer = Buffer.buffer();
        jsonArray.writeToBuffer(buffer);
        RpcResponse rpcResponse = RpcResponse.create("test", 200, buffer, 0);
        Assert.assertTrue(rpcResponse.isArray());
        Assert.assertNull(rpcResponse.responseObject());
        Assert.assertNotNull(rpcResponse.responseArray());
        Assert.assertEquals(jsonArray.encode(), rpcResponse.responseArray().encode());
    }

    @Test
    public void testInvalidJson() {
        Buffer buffer = Buffer.buffer("foo");
        try {
            RpcResponse.create("test", 200, buffer, 0);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof SystemException);
            SystemException ex = (SystemException) e;
            Assert.assertEquals(1024, ex.getErrorCode().getNumber());
        }
    }

    @Test
    public void testInvalidJsonObject() {
        Buffer buffer = Buffer.buffer("{foo}");
        try {
            RpcResponse.create("test", 200, buffer, 0);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof SystemException);
            SystemException ex = (SystemException) e;
            Assert.assertEquals(1024, ex.getErrorCode().getNumber());
        }
    }

    @Test
    public void testInvalidJsonArray() {
        Buffer buffer = Buffer.buffer("[{]");
        try {
            RpcResponse.create("test", 200, buffer, 0);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof SystemException);
            SystemException ex = (SystemException) e;
            Assert.assertEquals(1024, ex.getErrorCode().getNumber());
        }
    }
}
