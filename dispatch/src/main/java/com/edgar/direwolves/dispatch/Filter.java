package com.edgar.direwolves.dispatch;

/**
 * Created by edgar on 16-9-18.
 */
public interface Filter {

    int order();

    boolean shouldFilter(String apiName,ApiContext apiContext);

    void doFilter(String apiName, ApiContext apiContext);
}
