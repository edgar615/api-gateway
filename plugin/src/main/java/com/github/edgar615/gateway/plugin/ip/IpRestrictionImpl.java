package com.github.edgar615.gateway.plugin.ip;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
class IpRestrictionImpl implements IpRestriction {
  /**
   * 白名单
   */
  private final Set<String> whitelist = new HashSet<>();

  /**
   * 黑名单
   */
  private final Set<String> blacklist = new HashSet<>();

  IpRestrictionImpl() {
  }

  @Override
  public IpRestriction addWhitelist(String ip) {
    Preconditions.checkNotNull(ip, "ip cannot be null");
    blacklist.remove(ip);
    whitelist.add(ip);
    return this;
  }

  @Override
  public IpRestriction addBlacklist(String ip) {
    Preconditions.checkNotNull(ip, "ip cannot be null");
    whitelist.remove(ip);
    blacklist.add(ip);
    return this;
  }

  @Override
  public IpRestriction removeWhitelist(String ip) {
    Preconditions.checkNotNull(ip, "ip cannot be null");
    whitelist.remove(ip);
    return this;
  }

  public IpRestriction removeBlacklist(String ip) {
    Preconditions.checkNotNull(ip, "ip cannot be null");
    blacklist.remove(ip);
    return this;
  }

  @Override
  public IpRestriction clearWhitelist() {
    whitelist.clear();
    return this;
  }

  @Override
  public IpRestriction clearBlacklist() {
    blacklist.clear();
    return this;
  }

  @Override
  public List<String> whitelist() {
    return ImmutableList.copyOf(whitelist);
  }

  @Override
  public List<String> blacklist() {
    return ImmutableList.copyOf(blacklist);
  }

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper("IpRestriction")
            .add("whitelist", whitelist)
            .add("blacklist", blacklist)
            .toString();
  }
}
