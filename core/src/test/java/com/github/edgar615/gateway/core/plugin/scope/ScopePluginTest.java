package com.github.edgar615.gateway.core.plugin.scope;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public class ScopePluginTest {
    @Test
    public void testDecode() {
        JsonObject config = new JsonObject()
                .put("scope", "user.read");
        ApiPluginFactory factory = new ScopePluginFactory();
        ScopePlugin plugin = (ScopePlugin) factory.decode(config);
        Assert.assertNotNull(plugin);
        Assert.assertEquals("user.read", plugin.scope());
    }

    @Test
    public void testEncode() {
        ScopePluginImpl plugin = (ScopePluginImpl) ApiPlugin.create(ScopePlugin
                                                                                      .class
                                                                                      .getSimpleName());
        plugin.setPermission("user.read");
        JsonObject jsonObject = plugin.encode();
        System.out.println(jsonObject);
        Assert.assertEquals("user.read", jsonObject.getString("scope"));
    }

    @Test
    public void testNullShoudReturnEmptyJson() {
        ApiPluginFactory factory = new ScopePluginFactory();
        JsonObject jsonObject = factory.encode(null);
        Assert.assertTrue(jsonObject.isEmpty());
    }

}
