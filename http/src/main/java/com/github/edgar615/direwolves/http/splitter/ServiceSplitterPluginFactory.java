package com.github.edgar615.direwolves.http.splitter;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ServiceSplitterPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return ServiceSplitterPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new ServiceSplitterPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getValue("service.splitter") instanceof JsonObject) {
      JsonObject splitter = jsonObject.getJsonObject("service.splitter");
      ServiceSplitterPlugin plugin = new ServiceSplitterPlugin();
      for (String service : splitter.fieldNames()) {
        JsonObject serviceSplitterJson = splitter.getJsonObject(service);
        if (serviceSplitterJson.getValue("policy") instanceof String) {
          String policy = serviceSplitterJson.getString("policy");
          if ("ip".equalsIgnoreCase(policy)) {
            //根据IP的匹配
            plugin.addTraffic(service,new ClientIpTraffic(ipPolicies(serviceSplitterJson)));
          }
        }
      }
      if (plugin.traffics().isEmpty()) {
        return null;
      }
      return plugin;
    }

    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    ServiceSplitterPlugin serviceSplitterPlugin = (ServiceSplitterPlugin) plugin;

    if (serviceSplitterPlugin == null) {
      return new JsonObject();
    }
    JsonObject jsonObject = new JsonObject();
    for (String service : serviceSplitterPlugin.traffics().keySet()) {
      if (serviceSplitterPlugin.traffic(service) instanceof ClientIpTraffic) {
        JsonObject splitterJson = new JsonObject();
        splitterJson.put("policy", "ip");
        ClientIpTraffic clientIpTraffic = (ClientIpTraffic) serviceSplitterPlugin.traffic(service);
        splitterJson.put("traffic", policyArray(clientIpTraffic.policies()));
        jsonObject.put(service, splitterJson);
      }
    }


    return new JsonObject().put("service.splitter", jsonObject);
  }

  private List<IpPolicy> ipPolicies(JsonObject jsonObject) {
    List<IpPolicy> ipPolicies = new ArrayList<>();
    JsonArray traffic = jsonObject.getJsonArray("traffic", new JsonArray());
    for (int i = 0; i < traffic.size(); i++) {
      JsonObject trafficJson = traffic.getJsonObject(i);
      String type = trafficJson.getString("type");
      String tag = trafficJson.getString("tag");
      if ("ip-hash".equalsIgnoreCase(type)) {
        int start = trafficJson.getInteger("start", 0);
        int end = trafficJson.getInteger("end", 100);
        ipPolicies.add(new IpHashPolicy(start, end, tag));
      } else if ("ip-range".equalsIgnoreCase(type)) {
        long start = trafficJson.getLong("start", 0l);
        long end = trafficJson.getLong("end", Long.MAX_VALUE);
        ipPolicies.add(new IpRangePolicy(start, end, tag));
      } else if ("ip-appoint".equalsIgnoreCase(type)) {
        JsonArray ipArray = trafficJson.getJsonArray("appoint", new JsonArray());
        IpAppointPolicy policy = new IpAppointPolicy(tag);
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
        trafficJson.put("tag", policy.serviceTag());
      }
      if (policies.get(i) instanceof IpRangePolicy) {
        IpRangePolicy policy = (IpRangePolicy) policies.get(i);
        trafficJson.put("type", "ip-range");
        trafficJson.put("start", policy.start());
        trafficJson.put("end", policy.end());
        trafficJson.put("tag", policy.serviceTag());
      }
      if (policies.get(i) instanceof IpAppointPolicy) {
        IpAppointPolicy policy = (IpAppointPolicy) policies.get(i);
        trafficJson.put("type", "ip-appoint");
        trafficJson.put("appoint", policy.appoint());
        trafficJson.put("tag", policy.serviceTag());
      }
    }
    return traffic;
  }

}
