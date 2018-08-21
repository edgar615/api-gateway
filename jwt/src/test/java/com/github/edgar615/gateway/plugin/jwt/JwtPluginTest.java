package com.github.edgar615.gateway.plugin.jwt;

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
public class JwtPluginTest {
    @Test
    public void testDecode() {
        JsonObject config = new JsonObject()
                .put("jwt.verify", true);
        ApiPluginFactory factory = new JwtPluginFactory();
        JwtPlugin plugin = (JwtPlugin) factory.decode(config);
        Assert.assertNotNull(plugin);
    }

    @Test
    public void testEncode() {
        JwtPlugin plugin = (JwtPlugin) ApiPlugin.create(JwtPlugin
                                                                .class
                                                                .getSimpleName());
        JsonObject jsonObject = plugin.encode();
        System.out.println(jsonObject);
        Assert.assertTrue(jsonObject.getBoolean("jwt.verify"));
    }

}
