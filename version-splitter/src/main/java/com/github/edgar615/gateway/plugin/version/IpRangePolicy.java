package com.github.edgar615.gateway.plugin.version;

import com.google.common.base.Preconditions;

import com.github.edgar615.util.net.IPUtils;

/**
 * ip范围.
 *
 * @author Edgar  Date 2018/2/10
 */
public class IpRangePolicy implements IpPolicy {

  /**
   * IP地址的开始值，用long表示
   */
  private final long start;

  /**
   * IP地址的结束值，用long表示
   */
  private final long end;

  /**
   * 版本号
   */
  private final String version;

  public IpRangePolicy(long start, long end, String version) {
    Preconditions.checkArgument(start <= end);
    Preconditions.checkArgument(start >= 0);
    Preconditions.checkArgument(end <= 4294967295l);
    Preconditions.checkNotNull(version);
    this.start = start;
    this.end = end;
    this.version = version;
  }

  public long start() {
    return start;
  }

  public long end() {
    return end;
  }

  @Override
  public String version() {
    return version;
  }

  @Override
  public boolean satisfy(String ip) {
    long ipNumber = IPUtils.ipToLong(ip);
    return ipNumber >= start && ipNumber <= end;
  }
}
