package com.edgar.direwolves.dispatch;

/**
 * Created by edgar on 16-9-18.
 */
public interface Filter {

    int order();

    boolean shouldFilter(ApiContext apiContext);

    void doFilter(ApiContext apiContext);
}
