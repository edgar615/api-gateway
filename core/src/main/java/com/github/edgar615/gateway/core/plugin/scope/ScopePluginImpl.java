package com.github.edgar615.gateway.core.plugin.scope;

import com.google.common.base.MoreObjects;

/**
 * Created by edgar on 16-12-25.
 */
public class ScopePluginImpl implements ScopePlugin {

    private String permission = "default";

    ScopePluginImpl() {
    }

    ScopePluginImpl(String scope) {
        this.permission = scope;
    }

    @Override
    public String scope() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper("ScopePlugin")
                .add("scope", permission)
                .toString();
    }
}
