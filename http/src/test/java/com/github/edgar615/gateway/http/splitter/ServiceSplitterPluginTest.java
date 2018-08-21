package com.github.edgar615.gateway.http.splitter;

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
public class ServiceSplitterPluginTest {

    @Test
    public void testIpDecode() {
        JsonObject jsonObject = new JsonObject();
        ApiPluginFactory factory = new ServiceSplitterPluginFactory();
        ServiceSplitterPlugin plugin = (ServiceSplitterPlugin) factory.decode(jsonObject);
        Assert.assertNull(plugin);

        JsonObject splitter = new JsonObject().put("policy", "ip");
        jsonObject = new JsonObject()
                .put("service.splitter", new JsonObject().put("device", splitter));
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonObject().put("type", "ip-hash").put("start", 5).put("end", 10).put
                ("tag", "v1"));
        jsonArray.add(new JsonObject().put("type", "ip-range").put("start", 135).put("end", 34343)
                              .put
                                      ("tag", "v2"));
        jsonArray.add(new JsonObject().put("type", "ip-appoint").put("appoint", new JsonArray().add
                ("192.*").add("127.0.0.1"))
                              .put
                                      ("tag", "v3"));
        splitter.put("traffic", jsonArray);
        System.out.println(jsonObject);
        plugin = (ServiceSplitterPlugin) factory.decode(jsonObject);
        Assert.assertTrue(plugin.traffics().containsKey("device"));
        ClientIpTraffic traffic = (ClientIpTraffic) plugin.traffic("device");
        Assert.assertTrue(traffic.policies().get(0) instanceof IpHashPolicy);
        IpHashPolicy ipHashPolicy = (IpHashPolicy) traffic.policies().get(0);
        Assert.assertEquals(5, ipHashPolicy.start());
        Assert.assertEquals(10, ipHashPolicy.end());
        Assert.assertEquals("v1", ipHashPolicy.serviceTag());

        Assert.assertTrue(traffic.policies().get(1) instanceof IpRangePolicy);
        IpRangePolicy ipRangePolicy = (IpRangePolicy) traffic.policies().get(1);
        Assert.assertEquals(135, ipRangePolicy.start());
        Assert.assertEquals(34343, ipRangePolicy.end());
        Assert.assertEquals("v2", ipRangePolicy.serviceTag());

        Assert.assertTrue(traffic.policies().get(2) instanceof IpAppointPolicy);
        IpAppointPolicy ipAppointPolicy = (IpAppointPolicy) traffic.policies().get(2);
        Assert.assertEquals(2, ipAppointPolicy.appoint().size());
        Assert.assertEquals("v3", ipAppointPolicy.serviceTag());
    }

    @Test
    public void testEncode() {
        ApiPluginFactory factory = new ServiceSplitterPluginFactory();
        JsonObject splitter = new JsonObject().put("policy", "ip");
        JsonObject jsonObject = new JsonObject()
                .put("service.splitter", new JsonObject().put("device", splitter));
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonObject().put("type", "ip-hash").put("start", 5).put("end", 10).put
                ("tag", "v1"));
        jsonArray.add(new JsonObject().put("type", "ip-range").put("start", 135).put("end", 34343)
                              .put
                                      ("tag", "v2"));
        jsonArray.add(new JsonObject().put("type", "ip-appoint").put("appoint", new JsonArray().add
                ("192.*").add("127.0.0.1"))
                              .put
                                      ("tag", "v3"));
        splitter.put("traffic", jsonArray);
        System.out.println(jsonObject);
        ServiceSplitterPlugin plugin = (ServiceSplitterPlugin) factory.decode(jsonObject);
        JsonObject newJson = plugin.encode();
        System.out.println(newJson);
        Assert.assertTrue(newJson.containsKey("service.splitter"));
        JsonObject newSplitter = newJson.getJsonObject("service.splitter");
        Assert.assertTrue(newSplitter.containsKey("device"));
    }
}
