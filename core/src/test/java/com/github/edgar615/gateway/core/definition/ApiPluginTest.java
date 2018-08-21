package com.github.edgar615.gateway.core.definition;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Created by Edgar on 2017/1/6.
 *
 * @author Edgar  Date 2017/1/6
 */
public class ApiPluginTest {

    @Test
    public void testCreate() {
        ApiPlugin plugin = ApiPlugin.create(MockPlugin.class.getSimpleName());
        Assert.assertNotNull(plugin);
        Assert.assertTrue(plugin instanceof MockPlugin);
    }

    @Test
    public void testUndefinedPluginShouldThrowNoSuchElement() {
        try {
            ApiPlugin.create(UUID.randomUUID().toString());
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NoSuchElementException);
        }
    }

    @Test
    public void testEncode() {
        ApiPlugin plugin = ApiPlugin.create(MockPlugin.class.getSimpleName());
        JsonObject jsonObject = plugin.encode();
        Assert.assertTrue(jsonObject.getBoolean("mock"));
    }
}
