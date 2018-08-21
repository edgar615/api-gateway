package com.github.edgar615.gateway.plugin.auth;

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
public class AuthenticationPluginTest {
    @Test
    public void testDecode() {
        JsonObject config = new JsonObject()
                .put("authentication", true);
        ApiPluginFactory factory = new AuthenticationPluginFactory();
        AuthenticationPlugin plugin = (AuthenticationPlugin) factory.decode(config);
        Assert.assertNotNull(plugin);
    }

    @Test
    public void testEncode() {
        AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
                                                                    .class
                                                                    .getSimpleName());
        JsonObject jsonObject = plugin.encode();
        System.out.println(jsonObject);
        Assert.assertTrue(jsonObject.getBoolean("authentication"));
    }

}
