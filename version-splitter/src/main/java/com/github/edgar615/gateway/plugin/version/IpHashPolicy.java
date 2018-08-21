package com.github.edgar615.gateway.plugin.version;

import com.google.common.base.Preconditions;

import com.github.edgar615.util.net.IPUtils;

/**
 * ip范围.
 *
 * @author Edgar  Date 2018/2/10
 */
public class IpHashPolicy implements IpPolicy {

    /**
     * hash的开始值，最小值0,最大值100
     */
    private final int start;

    /**
     * hash的结束值，最小值0,最大值100
     */
    private final int end;

    /**
     * 版本号
     */
    private final String version;

    public IpHashPolicy(int start, int end, String version) {
        Preconditions.checkArgument(start <= end);
        Preconditions.checkArgument(start >= 0);
        Preconditions.checkArgument(end <= 100);
        Preconditions.checkNotNull(version);
        this.start = start;
        this.end = end;
        this.version = version;
    }

    public long start() {
        return start;
    }

    public long end() {
        return end;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public boolean satisfy(String ip) {
        long ipNumber = IPUtils.ipToLong(ip);
        long ipHash = ipNumber % 100;
        return ipHash >= start && ipHash <= end;
    }
}
