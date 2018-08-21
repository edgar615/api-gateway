package com.github.edgar615.gateway.filter;

import com.github.edgar615.gateway.core.dispatch.Filter;

/**
 * Created by edgar on 17-3-10.
 */
public abstract class AbstractRequestReplaceFilter extends AbstractReplaceFilter implements Filter {
    @Override
    public String type() {
        return PRE;
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE - 1000;
    }

}
