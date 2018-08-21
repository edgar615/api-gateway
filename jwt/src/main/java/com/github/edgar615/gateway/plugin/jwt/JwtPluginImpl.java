package com.github.edgar615.gateway.plugin.jwt;

import com.google.common.base.MoreObjects;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
class JwtPluginImpl implements JwtPlugin {

    JwtPluginImpl() {
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper("JwtPlugin")
                .toString();
    }
}
