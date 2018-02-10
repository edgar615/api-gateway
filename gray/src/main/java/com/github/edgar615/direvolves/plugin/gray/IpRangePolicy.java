package com.github.edgar615.direvolves.plugin.gray;

import com.google.common.base.Preconditions;

/**
 * ip范围.
 *
 * @author Edgar  Date 2018/2/10
 */
public class IpRangePolicy {

  /**
   * IP地址的开始值，用long表示
   */
  private final long start;

  /**
   * IP地址的结束值，用long表示
   */
  private final long end;

  public IpRangePolicy(long start, long end) {
    Preconditions.checkArgument(start <= end);
    Preconditions.checkArgument(start >= 0);
    Preconditions.checkArgument(end <= 4294967295l);
    this.start = start;
    this.end = end;
  }

  public long start() {
    return start;
  }

  public long end() {
    return end;
  }
}
