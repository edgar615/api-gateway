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
//public class JsonToIpRestictionDefinitionTest {
//
//    @Test
//    public void testJson() {
//        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add.json");
//        IpRestrictionDefinition definition = JsonToIpRestrictionDefinition.instance().apply(addDeviceJson);
//        Assert.assertEquals(1, definition.blacklist().size());
//        Assert.assertEquals(2, definition.whitelist().size());
//    }
//
//    @Test
//    public void testJson2() {
//        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/ip_device_add.json");
//        IpRestrictionDefinition definition = JsonToIpRestrictionDefinition.instance().apply(addDeviceJson);
//        Assert.assertEquals(1, definition.blacklist().size());
//        Assert.assertEquals(2, definition.whitelist().size());
//    }
//
//    @Test
//    public void testJson3() {
//        JsonObject addDeviceJson = JsonUtils.getJsonFromFile("src/test/resources/device_add2.json");
//        IpRestrictionDefinition definition = JsonToIpRestrictionDefinition.instance().apply(addDeviceJson);
//        Assert.assertNull(definition);
//    }
//
//}
