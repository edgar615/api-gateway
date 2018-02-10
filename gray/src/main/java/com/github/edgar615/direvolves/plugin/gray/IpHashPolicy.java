package com.github.edgar615.direvolves.plugin.gray;

import com.google.common.base.Preconditions;

/**
 * ip范围.
 *
 * @author Edgar  Date 2018/2/10
 */
public class IpHashPolicy {

  /**
   * hash的开始值，最小值0,最大值100
   */
  private final int start;

  /**
   * hash的结束值，最小值0,最大值100
   */
  private final int end;

  public IpHashPolicy(int start, int end) {
    Preconditions.checkArgument(start <= end);
    Preconditions.checkArgument(start >= 0);
    Preconditions.checkArgument(end <= 100);
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
