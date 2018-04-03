package com.github.edgar615.direwolves.plugin.version;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class VersionSplitterPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return VersionSplitterPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new VersionSplitterPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getValue("version.splitter") instanceof JsonObject) {
      JsonObject splitter = jsonObject.getJsonObject("version.splitter");

      String unSatisfyStrategy = splitter.getString("unSatisfyStrategy", "floor");
//      if ("floor".equalsIgnoreCase(type)) {
//        VersionMatchPlugin plugin = new VersionMatchPlugin();
//        plugin.floor();
//        return plugin;
//      }
//      if ("ceil".equalsIgnoreCase(type)) {
//        VersionMatchPlugin plugin = new VersionMatchPlugin();
//        plugin.ceil();
//        return plugin;
//      }
      if (splitter.getValue("policy") instanceof String) {
        String policy = splitter.getString("policy");
        if ("request-version".equalsIgnoreCase(policy)) {
          //根据请求头指定的版本号匹配
          return new VersionSplitterPlugin(unSatisfyStrategy, new RequestVersionTraffic());
        } else if ("ip".equalsIgnoreCase(policy)) {
          //根据IP的匹配
          return new VersionSplitterPlugin(unSatisfyStrategy, new ClientIpTraffic(
                  ipPolicies(splitter)));
        }
      }
    }

    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    VersionSplitterPlugin versionSplitterPlugin = (VersionSplitterPlugin) plugin;

    if (versionSplitterPlugin == null) {
      return new JsonObject();
    }
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("unSatisfyStrategy", versionSplitterPlugin.unSatisfyStrategy());
    if (versionSplitterPlugin.traffic() instanceof RequestVersionTraffic) {
      jsonObject.put("policy", "request-version");
    } else if (versionSplitterPlugin.traffic() instanceof ClientIpTraffic) {
      jsonObject.put("policy", "ip");
      ClientIpTraffic clientIpTraffic = (ClientIpTraffic) versionSplitterPlugin.traffic();
      jsonObject.put("traffic", policyArray(clientIpTraffic.policies()));
    }

    return new JsonObject().put("version.splitter", jsonObject);
  }

  private List<IpPolicy> ipPolicies(JsonObject jsonObject) {
    List<IpPolicy> ipPolicies = new ArrayList<>();
    JsonArray traffic = jsonObject.getJsonArray("traffic", new JsonArray());
    for (int i = 0; i < traffic.size(); i++) {
      JsonObject trafficJson = traffic.getJsonObject(i);
      String type = trafficJson.getString("type");
      String version = trafficJson.getString("version");
      if ("ip-hash".equalsIgnoreCase(type)) {
        int start = trafficJson.getInteger("start", 0);
        int end = trafficJson.getInteger("end", 100);
        ipPolicies.add(new IpHashPolicy(start, end, version));
      } else if ("ip-range".equalsIgnoreCase(type)) {
        long start = trafficJson.getLong("start", 0l);
        long end = trafficJson.getLong("end", Long.MAX_VALUE);
        ipPolicies.add(new IpRangePolicy(start, end, version));
      } else if ("ip-appoint".equalsIgnoreCase(type)) {
        JsonArray ipArray = trafficJson.getJsonArray("appoint", new JsonArray());
        IpAppointPolicy policy = new IpAppointPolicy(version);
        ipArray.stream().map(ip -> (String) ip)
                .forEach(ip -> policy.addIp(ip));
        ipPolicies.add(policy);
      }
    }
    return ipPolicies;
  }

  private JsonArray policyArray(List<IpPolicy> policies) {
    JsonArray traffic = new JsonArray();
    for (int i = 0; i < policies.size(); i++) {
      JsonObject trafficJson = new JsonObject();
      traffic.add(trafficJson);
      if (policies.get(i) instanceof IpHashPolicy) {
        IpHashPolicy policy = (IpHashPolicy) policies.get(i);
        trafficJson.put("type", "ip-hash");
        trafficJson.put("start", policy.start());
        trafficJson.put("end", policy.end());
        trafficJson.put("version", policy.version());
      }
      if (policies.get(i) instanceof IpRangePolicy) {
        IpRangePolicy policy = (IpRangePolicy) policies.get(i);
        trafficJson.put("type", "ip-range");
        trafficJson.put("start", policy.start());
        trafficJson.put("end", policy.end());
        trafficJson.put("version", policy.version());
      }
      if (policies.get(i) instanceof IpAppointPolicy) {
        IpAppointPolicy policy = (IpAppointPolicy) policies.get(i);
        trafficJson.put("type", "ip-appoint");
        trafficJson.put("appoint", policy.appoint());
        trafficJson.put("version", policy.version());
      }
    }
    return traffic;
  }

}
