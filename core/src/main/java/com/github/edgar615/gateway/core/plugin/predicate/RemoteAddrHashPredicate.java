package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.util.net.IPUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RemoteAddrHashPredicate implements ApiPredicate {
  /**
   * hash的开始值，最小值0,最大值100
   */
  private final int start;

  /**
   * hash的结束值，最小值0,最大值100
   */
  private final int end;

  public RemoteAddrHashPredicate(int start, int end) {
    Preconditions.checkArgument(start <= end);
    Preconditions.checkArgument(start >= 0);
    Preconditions.checkArgument(end <= 100);
    this.start = start;
    this.end = end;
  }

  public boolean test(ApiContext context) {
    String clientIp = (String) context.variables().get("request_clientIp");
    if (Strings.isNullOrEmpty(clientIp)) {
      return false;
    }
    long ipNumber = IPUtils.ipToLong(clientIp);
    long ipHash = ipNumber % 100;
    return ipHash >= start && ipHash <= end;
  }

}
