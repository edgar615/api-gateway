package com.edgar.direwolves.plugin.acl;

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
class AclRestrictionPluginImpl implements AclRestrictionPlugin {
  /**
   * 白名单
   */
  private final Set<String> whitelist = new HashSet<>();

  /**
   * 黑名单
   */
  private final Set<String> blacklist = new HashSet<>();

  AclRestrictionPluginImpl() {
  }

  @Override
  public AclRestrictionPlugin addWhitelist(String group) {
    Preconditions.checkNotNull(group, "group cannot be null");
    Preconditions.checkArgument(whitelist.size() <= 100, "whitelist must <= 100");
    blacklist.remove(group);
    whitelist.add(group);
    return this;
  }

  @Override
  public AclRestrictionPlugin addBlacklist(String group) {
    Preconditions.checkNotNull(group, "group cannot be null");
    Preconditions.checkArgument(blacklist.size() <= 100, "blacklist must <= 100");
    whitelist.remove(group);
    blacklist.add(group);
    return this;
  }

  @Override
  public AclRestrictionPlugin removeWhitelist(String group) {
    Preconditions.checkNotNull(group, "group cannot be null");
    whitelist.remove(group);
    return this;
  }

  @Override
  public AclRestrictionPlugin removeBlacklist(String group) {
    Preconditions.checkNotNull(group, "group cannot be null");
    blacklist.remove(group);
    return this;
  }

  @Override
  public AclRestrictionPlugin clearWhitelist() {
    whitelist.clear();
    return this;
  }

  @Override
  public AclRestrictionPlugin clearBlacklist() {
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
