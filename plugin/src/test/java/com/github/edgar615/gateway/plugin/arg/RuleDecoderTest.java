package com.github.edgar615.gateway.plugin.arg;

import com.github.edgar615.util.validation.Rule;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by Edgar on 2016/9/13.
 *
 * @author Edgar  Date 2016/9/13
 */
public class RuleDecoderTest {

  @Test
  public void testRuleDecoder1() {
    JsonObject jsonObject = new JsonObject()
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
    List<Rule> rules = RulesDecoder.instance().apply(jsonObject);
    System.out.println(rules);
    Assert.assertEquals(13, rules.size());
  }

  @Test
  public void testRuleDecoder2() {
    JsonObject jsonObject = new JsonObject()
            .put("required", "abc")
            .put("prohibited", false)
            .put("email", false)
            .put("integer", false)
            .put("bool", false)
            .put("list", false)
            .put("map", false)
            .put("max_length", 10)
            .put("maxLength", 10)
            .put("min_length", 10)
            .put("minLength", 10)
            .put("max", 10)
            .put("min", 10)
            .put("regex", "/devices")
            .put("optional", "1,2,3");
    List<Rule> rules = RulesDecoder.instance().apply(jsonObject);
    Assert.assertEquals(6, rules.size());
  }

  @Test
  public void testRuleDecoder3() {
    JsonObject jsonObject = new JsonObject();
    List<Rule> rules = RulesDecoder.instance().apply(jsonObject);
    Assert.assertEquals(0, rules.size());
  }

}
