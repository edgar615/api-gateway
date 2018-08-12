package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RemoteAddrAppointPredicate implements ApiPredicate {
  /**
   * IP的集合
   */
  private final List<String> appoint = new ArrayList<>();

  public RemoteAddrAppointPredicate(List<String> appoint) {
    Objects.requireNonNull(appoint);
    this.appoint.addAll(appoint);
  }

  public boolean test(ApiContext context) {
    String clientIp = (String) context.variables().get("request_clientIp");
    if (Strings.isNullOrEmpty(clientIp)) {
      return false;
    }
    return appoint.stream()
            .filter(r -> checkIp(r, clientIp))
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
