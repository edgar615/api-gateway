package com.github.edgar615.direvolves.plugin.gray;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Edgar on 2018/2/10.
 *
 * @author Edgar  Date 2018/2/10
 */
public class ServiceSplitter {

  private final String service;

  private List<IpSplitPolicy> policies = new ArrayList<>();

  public ServiceSplitter(String service) {this.service = service;}

  public ServiceSplitter addPolicy(IpSplitPolicy policy) {
    this.policies.add(policy);
    return this;
  }

  public String split(String ip) {
    Optional<IpSplitPolicy> optional = policies.stream()
            .filter(p -> p.satisfy(ip))
            .findFirst();
    if (optional.isPresent()) {
      return service;
    }
    return optional.get().service();
  }
}
