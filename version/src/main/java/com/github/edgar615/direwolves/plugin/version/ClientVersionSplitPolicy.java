package com.github.edgar615.direwolves.plugin.version;

/**
 * Created by Edgar on 2018/2/10.
 *
 * @author Edgar  Date 2018/2/10
 */
public interface ClientVersionSplitPolicy {

  String version();

  boolean satisfy(String clientIp);
}
