package com.github.edgar615.gateway.core.plugin.predicate;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.util.net.IPUtils;

public class RemoteAddrRangePredicate implements ApiPredicate {
    /**
     * IP地址的开始值，用long表示
     */
    private final long start;

    /**
     * IP地址的结束值，用long表示
     */
    private final long end;


    public RemoteAddrRangePredicate(long start, long end) {
        Preconditions.checkArgument(start <= end);
        Preconditions.checkArgument(start >= 0);
        Preconditions.checkArgument(end <= 4294967295l);
        this.start = start;
        this.end = end;
    }

    public boolean test(ApiContext context) {
        String clientIp = (String) context.variables().get("request_clientIp");
        if (Strings.isNullOrEmpty(clientIp)) {
            return false;
        }
        long ipNumber = IPUtils.ipToLong(clientIp);
        return ipNumber >= start && ipNumber <= end;
    }

    public long start() {
        return start;
    }

    public long end() {
        return end;
    }
}
