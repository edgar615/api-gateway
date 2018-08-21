package com.github.edgar615.gateway.core.plugin.order;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class OrderPluginTest {

    @Test
    public void testDecode() {
        JsonObject jsonObject = new JsonObject();
        ApiPluginFactory factory = new OrderPluginFactory();
        ApiPlugin plugin = factory.decode(jsonObject);
        Assert.assertNull(plugin);
        jsonObject.put("order", 10);
        plugin = factory.decode(jsonObject);
        Assert.assertTrue(plugin instanceof OrderPlugin);
        Assert.assertEquals(((OrderPlugin) plugin).order(), 10);
    }

    @Test
    public void testEncode() {
        OrderPlugin orderPlugin = new OrderPlugin(10);
        ApiPluginFactory factory = new OrderPluginFactory();
        JsonObject jsonObject = factory.encode(orderPlugin);
        Assert.assertEquals(jsonObject.getValue("order"), 10);
    }
}
