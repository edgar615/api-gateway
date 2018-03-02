package com.github.edgar615.direvolves.plugin.gray;

import com.google.common.base.Preconditions;

import com.github.edgar615.util.net.IPUtils;

/**
 * ip范围.
 *
 * @author Edgar  Date 2018/2/10
 */
public class IpHashPolicy implements IpSplitPolicy {

  /**
   * hash的开始值，最小值0,最大值100
   */
  private final int start;

  /**
   * hash的结束值，最小值0,最大值100
   */
  private final int end;

  /**
   * 新服务名
   */
  private final String service;

  public IpHashPolicy(int start, int end, String service) {
    Preconditions.checkArgument(start <= end);
    Preconditions.checkArgument(start >= 0);
    Preconditions.checkArgument(end <= 100);
    Preconditions.checkNotNull(service);
    this.start = start;
    this.end = end;
    this.service = service;
  }

  public long start() {
    return start;
  }

  public long end() {
    return end;
  }

  @Override
  public String service() {
    return service;
  }

  @Override
  public boolean satisfy(String ip) {
    long ipNumber = IPUtils.ipToLong(ip);
    long ipHash = ipNumber % 100;
    return ipHash >= start && ipHash <= end;
  }
}
