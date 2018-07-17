package com.github.edgar615.gateway.plugin.user;

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
class UserRestrictionImpl implements UserRestriction {
  /**
   * 白名单
   */
  private final Set<String> whitelist = new HashSet<>();

  /**
   * 黑名单
   */
  private final Set<String> blacklist = new HashSet<>();

  UserRestrictionImpl() {
  }

  @Override
  public UserRestriction addWhitelist(String userId) {
    Preconditions.checkNotNull(userId, "userId cannot be null");
    blacklist.remove(userId);
    whitelist.add(userId);
    return this;
  }

  @Override
  public UserRestriction addBlacklist(String userId) {
    Preconditions.checkNotNull(userId, "userId cannot be null");
    whitelist.remove(userId);
    blacklist.add(userId);
    return this;
  }

  @Override
  public UserRestriction removeWhitelist(String userId) {
    Preconditions.checkNotNull(userId, "userId cannot be null");
    whitelist.remove(userId);
    return this;
  }

  @Override
  public UserRestriction removeBlacklist(String userId) {
    Preconditions.checkNotNull(userId, "userId cannot be null");
    blacklist.remove(userId);
    return this;
  }

  @Override
  public UserRestriction clearWhitelist() {
    whitelist.clear();
    return this;
  }

  @Override
  public UserRestriction clearBlacklist() {
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
