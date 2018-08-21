package com.github.edgar615.gateway.plugin.auth;

import com.google.common.base.MoreObjects;

/**
 * Created by edgar on 16-12-25.
 */
public class PermissionPluginImpl implements PermissionPlugin {

    private String permission = "default";

    PermissionPluginImpl() {
    }

    PermissionPluginImpl(String scope) {
        this.permission = scope;
    }

    @Override
    public String permission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper("PermissionPlugin")
                .add("permission", permission)
                .toString();
    }
}
