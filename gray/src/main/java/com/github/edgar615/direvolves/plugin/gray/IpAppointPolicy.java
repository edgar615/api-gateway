package com.github.edgar615.direvolves.plugin.gray;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * 特定IP.
 *
 * @author Edgar  Date 2018/2/10
 */
public class IpAppointPolicy implements IpSplitPolicy {

  /**
   * IP的集合
   */
  private final List<String> ipList = new ArrayList<>();

  /**
   * 新服务名
   */
  private final String service;

  public IpAppointPolicy(String service) {this.service = service;}

  public IpAppointPolicy addIp(String ip) {
    this.ipList.add(ip);
    return this;
  }

  public List<String> ipList() {
    return ipList;
  }

  @Override
  public String service() {
    return service;
  }

  @Override
  public boolean satisfy(String ip) {
    return ipList.stream()
                   .filter(r -> checkIp(r, ip))
                   .count() > 0;
  }

  private boolean checkIp(String rule, String clientIp) {
    List<String> rules = Lists.newArrayList(Splitter.on(".").trimResults().split(rule));
    for (int i = rules.size(); i < 5; i++) {
      rules.add("*");
    }
    List<String> ips = Lists.newArrayList(Splitter.on(".").trimResults().split(clientIp));
    for (int i = 0; i < 4; i++) {
      String r = rules.get(i);
      String ip = ips.get(i);
      if (!r.equals(ip) && !"*".equals(r)) {
        return false;
      }
    }
    return true;
  }
}
