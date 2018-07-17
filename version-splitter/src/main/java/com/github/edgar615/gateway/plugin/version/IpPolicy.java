package com.github.edgar615.gateway.plugin.version;

/**
 * Created by Edgar on 2018/2/10.
 *
 * @author Edgar  Date 2018/2/10
 */
public interface IpPolicy {

  String version();

  boolean satisfy(String ip);
}
