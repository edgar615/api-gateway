package com.github.edgar615.gateway.plugin.arg;

import com.github.edgar615.gateway.core.definition.ApiPlugin;

/**
 * Created by Edgar on 2017/3/30.
 *
 * @author Edgar  Date 2017/3/30
 */
public class StrictArgPlugin implements ApiPlugin {

    private final boolean strict;

    public StrictArgPlugin(boolean strict) {this.strict = strict;}

    @Override
    public String name() {
        return StrictArgPlugin.class.getSimpleName();
    }

    public boolean strict() {
        return strict;
    }
}
