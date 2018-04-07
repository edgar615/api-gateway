package com.github.edgar615.direwolves.http.splitter;

import com.github.edgar615.util.net.IPUtils;
import com.google.common.base.Preconditions;

/**
 * ip范围.
 *
 * @author Edgar  Date 2018/2/10
 */
public class IpHashPolicy implements IpPolicy {

  /**
   * hash的开始值，最小值0,最大值100
   */
  private final int start;

  /**
   * hash的结束值，最小值0,最大值100
   */
  private final int end;

  /**
   * 版本号
   */
  private final String tag;

  public IpHashPolicy(int start, int end, String tag) {
    Preconditions.checkArgument(start <= end);
    Preconditions.checkArgument(start >= 0);
    Preconditions.checkArgument(end <= 100);
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
    long ipHash = ipNumber % 100;
    return ipHash >= start && ipHash <= end;
  }
}
