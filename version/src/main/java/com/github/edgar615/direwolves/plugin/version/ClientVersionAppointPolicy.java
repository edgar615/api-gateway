package com.github.edgar615.direwolves.plugin.version;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * 特定版本.
 *
 * @author Edgar  Date 2018/2/10
 */
public class ClientVersionAppointPolicy implements IpSplitPolicy {

  /**
   * 版本的集合
   */
  private final List<String> clientVersionList = new ArrayList<>();

  /**
   * 版本号
   */
  private final String version;

  public ClientVersionAppointPolicy(String version) {this.version = version;}

  public ClientVersionAppointPolicy addClientVersion(String ip) {
    this.clientVersionList.add(ip);
    return this;
  }

  public List<String> clientVersionList() {
    return clientVersionList;
  }

  @Override
  public String version() {
    return version;
  }

  @Override
  public boolean satisfy(String ip) {
    return clientVersionList.stream()
                   .filter(r -> checkClientVersion(r, ip))
                   .count() > 0;
  }

  private boolean checkClientVersion(String rule, String clientVersion) {
    return rule.equals(clientVersion);
  }
}
