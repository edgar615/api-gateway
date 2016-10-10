package com.edgar.direwolves.definition;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/9/13.
 *
 * @author Edgar  Date 2016/9/13
 */
public class ParameterDecoderTest {

  @Test
  public void testRuleDecoder1() {
    JsonObject ruleJson = new JsonObject()
            .put("required", true)
            .put("prohibited", true)
            .put("email", true)
            .put("integer", true)
            .put("bool", true)
            .put("list", true)
            .put("map", true)
            .put("max_length", 10)
            .put("maxLength", 10)
            .put("min_length", 10)
            .put("minLength", 10)
            .put("max", 10)
            .put("min", 10)
            .put("regex", "/devices")
            .put("optional", new JsonArray().add(1).add(2).add(3));

    JsonObject jsonObject = new JsonObject()
            .put("rules", ruleJson)
            .put("name", "name")
            .put("default_value", "edgar");
    Parameter parameter = ParameterDecoder.instance().apply(jsonObject);
    Assert.assertEquals(13, parameter.rules().size());
    Assert.assertEquals("edgar", parameter.defaultValue());
    Assert.assertEquals("name", parameter.name());
  }

  @Test
  public void testRuleDecoder2() {
    JsonObject jsonObject = new JsonObject()
            .put("name", "name");
    Parameter parameter = ParameterDecoder.instance().apply(jsonObject);
    Assert.assertEquals(0, parameter.rules().size());
    Assert.assertNull(parameter.defaultValue());
    Assert.assertEquals("name", parameter.name());
  }
}
