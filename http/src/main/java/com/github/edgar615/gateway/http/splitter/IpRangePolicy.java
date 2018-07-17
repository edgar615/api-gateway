package com.github.edgar615.gateway.http.splitter;

import com.github.edgar615.util.net.IPUtils;
import com.google.common.base.Preconditions;

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
   * 服务标签
   */
  private final String tag;

  public IpRangePolicy(long start, long end, String tag) {
    Preconditions.checkArgument(start <= end);
    Preconditions.checkArgument(start >= 0);
    Preconditions.checkArgument(end <= 4294967295l);
    Preconditions.checkNotNull(tag);
    this.start = start;
    this.end = end;
    this.tag = tag;
  }

  public long start() {
    return start;
  }

  public long end() {
    return end;
  }

  @Override
  public String serviceTag() {
    return tag;
  }

  @Override
  public boolean satisfy(String ip) {
    long ipNumber = IPUtils.ipToLong(ip);
    return ipNumber >= start && ipNumber <= end;
  }
}
