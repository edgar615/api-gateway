package com.edgar.direwolves.definition;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.eb.ApiDefinitionCodec;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2016/9/13.
 *
 * @author Edgar  Date 2016/9/13
 */
@RunWith(VertxUnitRunner.class)
public class ApiDefinitionCodecTest {

  Vertx vertx;


  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();
  }

  @After
  public void clear(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void transformShouldNotSame(TestContext context) {
    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device")
            .put("method", "POST")
            .put("path", "/devices");
    JsonArray endpoints = new JsonArray()
            .add(new JsonObject().put("type", "http")
                         .put("name", "add_device")
                         .put("service", "device")
                         .put("method", "POST")
                         .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);

    ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
    ApiDefinition apiDefinition1 = new ApiDefinitionCodec().transform(apiDefinition);
    Assert.assertNotSame(apiDefinition, apiDefinition1);

  }

}
