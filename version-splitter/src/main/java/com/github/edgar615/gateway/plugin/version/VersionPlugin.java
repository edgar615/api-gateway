package com.github.edgar615.gateway.plugin.version;

import com.google.common.base.MoreObjects;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

/**
 * Created by Edgar on 2017/11/6.
 *
 * @author Edgar  Date 2017/11/6
 */
public class VersionPlugin implements ApiPlugin {

    private String version;

    @Override
    public String name() {
        return VersionPlugin.class.getSimpleName();
    }

    public String version() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(VersionPlugin.class)
                .add("version", version)
                .toString();
    }
}
