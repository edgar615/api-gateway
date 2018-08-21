package com.github.edgar615.gateway.plugin.appkey;

import com.google.common.base.MoreObjects;

/**
 * Created by edgar on 16-10-31.
 */
class AppKeyPluginImpl implements AppKeyPlugin {

    AppKeyPluginImpl() {

    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper("AppKeyPlugin")
                .toString();
    }

}
