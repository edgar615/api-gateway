package com.edgar.direwolves.plugin.ip;

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
class IpRestrictionPluginImpl implements IpRestrictionPlugin {
  /**
   * 白名单
   */
  private final Set<String> whitelist = new HashSet<>();

  /**
   * 黑名单
   */
  private final Set<String> blacklist = new HashSet<>();

  IpRestrictionPluginImpl() {
  }

  @Override
  public IpRestrictionPlugin addWhitelist(String ip) {
    Preconditions.checkNotNull(ip, "ip cannot be null");
    Preconditions.checkArgument(whitelist.size() <= 100, "whitelist must <= 100");
    blacklist.remove(ip);
    whitelist.add(ip);
    return this;
  }

  @Override
  public IpRestrictionPlugin addBlacklist(String ip) {
    Preconditions.checkNotNull(ip, "ip cannot be null");
    Preconditions.checkArgument(blacklist.size() <= 100, "blacklist must <= 100");
    whitelist.remove(ip);
    blacklist.add(ip);
    return this;
  }

  @Override
  public IpRestrictionPlugin removeWhitelist(String ip) {
    Preconditions.checkNotNull(ip, "ip cannot be null");
    whitelist.remove(ip);
    return this;
  }

  public IpRestrictionPlugin removeBlacklist(String ip) {
    Preconditions.checkNotNull(ip, "ip cannot be null");
    blacklist.remove(ip);
    return this;
  }

  @Override
  public IpRestrictionPlugin clearWhitelist() {
    whitelist.clear();
    return this;
  }

  @Override
  public IpRestrictionPlugin clearBlacklist() {
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

}
