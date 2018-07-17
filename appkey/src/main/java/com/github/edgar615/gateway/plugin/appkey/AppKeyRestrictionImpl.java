package com.github.edgar615.gateway.plugin.appkey;

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
class AppKeyRestrictionImpl implements AppKeyRestriction {
  /**
   * 白名单
   */
  private final Set<String> whitelist = new HashSet<>();

  /**
   * 黑名单
   */
  private final Set<String> blacklist = new HashSet<>();

  AppKeyRestrictionImpl() {
  }

  @Override
  public AppKeyRestriction addWhitelist(String appKey) {
    Preconditions.checkNotNull(appKey, "appKey cannot be null");
    blacklist.remove(appKey);
    whitelist.add(appKey);
    return this;
  }

  @Override
  public AppKeyRestriction addBlacklist(String appKey) {
    Preconditions.checkNotNull(appKey, "appKey cannot be null");
    whitelist.remove(appKey);
    blacklist.add(appKey);
    return this;
  }

  @Override
  public AppKeyRestriction removeWhitelist(String appKey) {
    Preconditions.checkNotNull(appKey, "appKey cannot be null");
    whitelist.remove(appKey);
    return this;
  }

  public AppKeyRestriction removeBlacklist(String appKey) {
    Preconditions.checkNotNull(appKey, "appKey cannot be null");
    blacklist.remove(appKey);
    return this;
  }

  @Override
  public AppKeyRestriction clearWhitelist() {
    whitelist.clear();
    return this;
  }

  @Override
  public AppKeyRestriction clearBlacklist() {
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
            .toStringHelper("AppKeyRestriction")
            .add("whitelist", whitelist)
            .add("blacklist", blacklist)
            .toString();
  }
}
