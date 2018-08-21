package com.github.edgar615.gateway.plugin.arg;

import com.google.common.base.MoreObjects;

/**
 * Created by edgar on 16-10-22.
 */
class BodyArgPluginImpl extends ArgPluginImpl implements BodyArgPlugin {

    BodyArgPluginImpl() {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("BodyArgPlugin")
                .add("parameters", parameters())
                .toString();
    }

}
