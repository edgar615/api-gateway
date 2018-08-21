package com.github.edgar615.gateway.http.splitter;

/**
 * Created by Edgar on 2018/2/10.
 *
 * @author Edgar  Date 2018/2/10
 */
public interface IpPolicy {

    String serviceTag();

    boolean satisfy(String ip);
}
