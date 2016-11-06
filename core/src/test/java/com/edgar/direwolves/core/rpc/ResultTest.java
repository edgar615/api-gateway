package com.edgar.direwolves.core.rpc;

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
public class ResultTest {

  @Test
  public void testEmptyJsonObject() {
    JsonObject jsonObject = new JsonObject();
    Buffer buffer = Buffer.buffer();
    jsonObject.writeToBuffer(buffer);
    Result result = Result.create("test", 200, buffer, 0);
    Assert.assertFalse(result.isArray());
    Assert.assertNotNull(result.responseObject());
    Assert.assertNull(result.responseArray());
    Assert.assertEquals("{}", result.responseObject().encode());
  }

  @Test
  public void testJsonObject() {
    JsonObject jsonObject = new JsonObject().put("foo", "bar");
    Buffer buffer = Buffer.buffer();
    jsonObject.writeToBuffer(buffer);
    Result result = Result.create("test", 200, buffer, 0);
    Assert.assertFalse(result.isArray());
    Assert.assertNotNull(result.responseObject());
    Assert.assertNull(result.responseArray());
    Assert.assertEquals(jsonObject.encode(), result.responseObject().encode());
  }

  @Test
  public void testEmptyJsonArray() {
    JsonArray jsonArray = new JsonArray();
    Buffer buffer = Buffer.buffer();
    jsonArray.writeToBuffer(buffer);
    Result result = Result.create("test", 200, buffer, 0);
    Assert.assertTrue(result.isArray());
    Assert.assertNull(result.responseObject());
    Assert.assertNotNull(result.responseArray());
    Assert.assertEquals("[]", result.responseArray().encode());
  }

  @Test
  public void testJsonArray() {
    JsonArray jsonArray = new JsonArray()
        .add(new JsonObject().put("foo", "bar"));
    Buffer buffer = Buffer.buffer();
    jsonArray.writeToBuffer(buffer);
    Result result = Result.create("test", 200, buffer, 0);
    Assert.assertTrue(result.isArray());
    Assert.assertNull(result.responseObject());
    Assert.assertNotNull(result.responseArray());
    Assert.assertEquals(jsonArray.encode(), result.responseArray().encode());
  }

  @Test
  public void testInvalidJson() {
    Buffer buffer = Buffer.buffer("foo");
    try {
      Result.create("test", 200, buffer, 0);
      Assert.fail();
    } catch (Exception e) {
      Assert.assertTrue(e instanceof SystemException);
      SystemException ex = (SystemException) e;
      Assert.assertEquals(1024, ex.getErrorCode().getNumber());
    }
  }
}
