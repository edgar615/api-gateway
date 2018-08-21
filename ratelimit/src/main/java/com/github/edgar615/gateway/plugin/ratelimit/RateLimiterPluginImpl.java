package com.github.edgar615.gateway.plugin.ratelimit;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
class RateLimiterPluginImpl implements RateLimiterPlugin {

    private final Set<RateLimiter> rateLimiters = new HashSet<>();

    @Override
    public List<RateLimiter> rateLimiters() {
        return ImmutableList.copyOf(rateLimiters);
    }

    @Override
    public void addRateLimiter(RateLimiter rateLimiter) {
        rateLimiters.removeIf(r -> r.name().equals(rateLimiter.name()));
        rateLimiters.add(rateLimiter);
    }

    @Override
    public void removeRateLimiter(String name) {
        rateLimiters.removeIf(r -> r.name().equals(name));
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper("RateLimiterPlugin")
                .add("rateLimiters", rateLimiters)
                .toString();
    }

}
