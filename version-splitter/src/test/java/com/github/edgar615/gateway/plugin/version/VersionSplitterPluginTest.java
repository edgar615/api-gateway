package com.github.edgar615.gateway.plugin.version;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public class VersionSplitterPluginTest {

    @Test
    public void testDecode() {
        JsonObject jsonObject = new JsonObject();
        ApiPluginFactory factory = new VersionSplitterPluginFactory();
        VersionSplitterPlugin plugin = (VersionSplitterPlugin) factory.decode(jsonObject);
        Assert.assertNull(plugin);

        jsonObject = new JsonObject()
                .put("version.splitter", new JsonObject().put("unSatisfyStrategy", "floor").put
                        ("policy", "request-version"));
        plugin = (VersionSplitterPlugin) factory.decode(jsonObject);
        Assert.assertEquals("floor", plugin.unSatisfyStrategy());
        Assert.assertTrue(plugin.traffic() instanceof RequestVersionTraffic);

        jsonObject = new JsonObject()
                .put("version.splitter", new JsonObject().put("unSatisfyStrategy", "ceil").put
                        ("policy", "request-version"));
        plugin = (VersionSplitterPlugin) factory.decode(jsonObject);
        Assert.assertEquals("ceil", plugin.unSatisfyStrategy());
        Assert.assertTrue(plugin.traffic() instanceof RequestVersionTraffic);
    }

    @Test
    public void testIpDecode() {
        JsonObject jsonObject = new JsonObject();
        ApiPluginFactory factory = new VersionSplitterPluginFactory();
        VersionSplitterPlugin plugin = (VersionSplitterPlugin) factory.decode(jsonObject);
        Assert.assertNull(plugin);

        JsonObject splitter = new JsonObject().put("unSatisfyStrategy", "floor").put
                ("policy", "ip");
        jsonObject = new JsonObject()
                .put("version.splitter", splitter);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonObject().put("type", "ip-hash").put("start", 5).put("end", 10).put
                ("version", "0.0.1"));
        jsonArray.add(new JsonObject().put("type", "ip-range").put("start", 135).put("end", 34343)
                              .put
                                      ("version", "0.0.2"));
        jsonArray.add(new JsonObject().put("type", "ip-appoint").put("appoint", new JsonArray().add
                ("192.*").add("127.0.0.1"))
                              .put
                                      ("version", "0.0.3"));
        splitter.put("traffic", jsonArray);
        plugin = (VersionSplitterPlugin) factory.decode(jsonObject);
        Assert.assertEquals("floor", plugin.unSatisfyStrategy());
        Assert.assertTrue(plugin.traffic() instanceof ClientIpTraffic);
        ClientIpTraffic traffic = (ClientIpTraffic) plugin.traffic();
        Assert.assertTrue(traffic.policies().get(0) instanceof IpHashPolicy);
        IpHashPolicy ipHashPolicy = (IpHashPolicy) traffic.policies().get(0);
        Assert.assertEquals(5, ipHashPolicy.start());
        Assert.assertEquals(10, ipHashPolicy.end());
        Assert.assertEquals("0.0.1", ipHashPolicy.version());

        Assert.assertTrue(traffic.policies().get(1) instanceof IpRangePolicy);
        IpRangePolicy ipRangePolicy = (IpRangePolicy) traffic.policies().get(1);
        Assert.assertEquals(135, ipRangePolicy.start());
        Assert.assertEquals(34343, ipRangePolicy.end());
        Assert.assertEquals("0.0.2", ipRangePolicy.version());

        Assert.assertTrue(traffic.policies().get(2) instanceof IpAppointPolicy);
        IpAppointPolicy ipAppointPolicy = (IpAppointPolicy) traffic.policies().get(2);
        Assert.assertEquals(2, ipAppointPolicy.appoint().size());
        Assert.assertEquals("0.0.3", ipAppointPolicy.version());
    }

    @Test
    public void testEncode() {
        ApiPlugin plugin = ApiPlugin.create(VersionSplitterPlugin.class.getSimpleName());
        VersionSplitterPlugin versionPlugin = (VersionSplitterPlugin) plugin;
        versionPlugin.floor(new RequestVersionTraffic());

        JsonObject jsonObject = versionPlugin.encode();
        System.out.println(jsonObject);
        Assert.assertTrue(jsonObject.containsKey("version.splitter"));
        JsonObject splitter = jsonObject.getJsonObject("version.splitter");
        Assert.assertEquals("floor", splitter.getString("unSatisfyStrategy"));
    }
}
