package com.github.edgar615.direvolves.plugin.gray;

import com.google.common.base.Preconditions;

import com.github.edgar615.util.net.IPUtils;

/**
 * ip范围.
 *
 * @author Edgar  Date 2018/2/10
 */
public class IpRangePolicy implements IpSplitPolicy {

  /**
   * IP地址的开始值，用long表示
   */
  private final long start;

  /**
   * IP地址的结束值，用long表示
   */
  private final long end;

  /**
   * 新服务名
   */
  private final String service;

  public IpRangePolicy(long start, long end, String service) {
    Preconditions.checkArgument(start <= end);
    Preconditions.checkArgument(start >= 0);
    Preconditions.checkArgument(end <= 4294967295l);
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
    return ipNumber >= start && ipNumber <= end;
  }
}
