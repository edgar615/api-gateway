//package com.edgar.direwolves.definition;
//
//import io.vertx.core.json.JsonObject;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.util.List;
//
///**
// * Created by Edgar on 2016/9/13.
// *
// * @author Edgar  Date 2016/9/13
// */
//public class JsonToRateLimitDefinitionTest {
//
//    @Test
//    public void testJson() {
//        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");
//        List<RateLimitDefinition> definitions =  JsonToRateLimitDefinition.instance().apply(addDeviceJson);
//        Assert.assertEquals(2, definitions.size());
//        Assert.assertEquals(RateLimitBy.TOKEN, definitions.get(0).rateLimitBy());
//    }
//
//    @Test
//    public void testJson2() {
//        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/rate_limit_device_add.json");
//        List<RateLimitDefinition> definitions =  JsonToRateLimitDefinition.instance().apply(addDeviceJson);
//        Assert.assertEquals(2, definitions.size());
//        Assert.assertEquals(RateLimitBy.APP_KEY, definitions.get(1).rateLimitBy());
//    }
//
//    @Test
//    public void testJson3() {
//        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add2.json");
//        List<RateLimitDefinition> definitions =  JsonToRateLimitDefinition.instance().apply(addDeviceJson);
//        Assert.assertEquals(0, definitions.size());
//    }
//
//}
