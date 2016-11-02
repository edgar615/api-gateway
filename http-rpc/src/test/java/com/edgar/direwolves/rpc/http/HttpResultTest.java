package com.edgar.direwolves.rpc.http;

import com.edgar.direwolves.core.rpc.HttpResult;
import com.edgar.util.exception.SystemException;
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
public class HttpResultTest {

  @Test
  public void testEmptyJsonObject() {
    JsonObject jsonObject = new JsonObject();
    Buffer buffer = Buffer.buffer();
    jsonObject.writeToBuffer(buffer);
    HttpResult httpResult = HttpResult.create("test", 200, buffer, 0);
    Assert.assertFalse(httpResult.isArray());
    Assert.assertNotNull(httpResult.responseObject());
    Assert.assertNull(httpResult.responseArray());
    Assert.assertEquals("{}", httpResult.responseObject().encode());
  }

  @Test
  public void testJsonObject() {
    JsonObject jsonObject = new JsonObject().put("foo", "bar");
    Buffer buffer = Buffer.buffer();
    jsonObject.writeToBuffer(buffer);
    HttpResult httpResult = HttpResult.create("test", 200, buffer, 0);
    Assert.assertFalse(httpResult.isArray());
    Assert.assertNotNull(httpResult.responseObject());
    Assert.assertNull(httpResult.responseArray());
    Assert.assertEquals(jsonObject.encode(), httpResult.responseObject().encode());
  }

  @Test
  public void testEmptyJsonArray() {
    JsonArray jsonArray = new JsonArray();
    Buffer buffer = Buffer.buffer();
    jsonArray.writeToBuffer(buffer);
    HttpResult httpResult = HttpResult.create("test", 200, buffer, 0);
    Assert.assertTrue(httpResult.isArray());
    Assert.assertNull(httpResult.responseObject());
    Assert.assertNotNull(httpResult.responseArray());
    Assert.assertEquals("[]", httpResult.responseArray().encode());
  }

  @Test
  public void testJsonArray() {
    JsonArray jsonArray = new JsonArray()
            .add(new JsonObject().put("foo", "bar"));
    Buffer buffer = Buffer.buffer();
    jsonArray.writeToBuffer(buffer);
    HttpResult httpResult = HttpResult.create("test", 200, buffer, 0);
    Assert.assertTrue(httpResult.isArray());
    Assert.assertNull(httpResult.responseObject());
    Assert.assertNotNull(httpResult.responseArray());
    Assert.assertEquals(jsonArray.encode(), httpResult.responseArray().encode());
  }

  @Test
  public void testInvalidJson() {
    Buffer buffer = Buffer.buffer("foo");
    try {
      HttpResult.create("test", 200, buffer, 0);
      Assert.fail();
    } catch (Exception e) {
      Assert.assertTrue(e instanceof SystemException);
      SystemException ex = (SystemException) e;
      Assert.assertEquals(1024, ex.getErrorCode().getNumber());
    }
  }
}
