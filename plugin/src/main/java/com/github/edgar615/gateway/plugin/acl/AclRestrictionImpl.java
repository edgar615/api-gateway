package com.github.edgar615.gateway.plugin.acl;

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
class AclRestrictionImpl implements AclRestriction {
  /**
   * 白名单
   */
  private final Set<String> whitelist = new HashSet<>();

  /**
   * 黑名单
   */
  private final Set<String> blacklist = new HashSet<>();

  AclRestrictionImpl() {
  }

  @Override
  public AclRestriction addWhitelist(String group) {
    Preconditions.checkNotNull(group, "group cannot be null");
    blacklist.remove(group);
    whitelist.add(group);
    return this;
  }

  @Override
  public AclRestriction addBlacklist(String group) {
    Preconditions.checkNotNull(group, "group cannot be null");
    whitelist.remove(group);
    blacklist.add(group);
    return this;
  }

  @Override
  public AclRestriction removeWhitelist(String group) {
    Preconditions.checkNotNull(group, "group cannot be null");
    whitelist.remove(group);
    return this;
  }

  @Override
  public AclRestriction removeBlacklist(String group) {
    Preconditions.checkNotNull(group, "group cannot be null");
    blacklist.remove(group);
    return this;
  }

  @Override
  public AclRestriction clearWhitelist() {
    whitelist.clear();
    return this;
  }

  @Override
  public AclRestriction clearBlacklist() {
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
            .toStringHelper("UserRestriction")
            .add("whitelist", whitelist)
            .add("blacklist", blacklist)
            .toString();
  }
}
