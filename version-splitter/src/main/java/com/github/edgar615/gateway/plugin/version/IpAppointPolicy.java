package com.github.edgar615.gateway.plugin.version;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * 特定IP.
 *
 * @author Edgar  Date 2018/2/10
 */
public class IpAppointPolicy implements IpPolicy {

  /**
   * IP的集合
   */
  private final List<String> appoint = new ArrayList<>();

  /**
   * 版本号
   */
  private final String version;

  public IpAppointPolicy(String version) {this.version = version;}

  public IpAppointPolicy addIp(String ip) {
    this.appoint.add(ip);
    return this;
  }

  public List<String> appoint() {
    return appoint;
  }

  @Override
  public String version() {
    return version;
  }

  @Override
  public boolean satisfy(String ip) {
    return appoint.stream()
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
