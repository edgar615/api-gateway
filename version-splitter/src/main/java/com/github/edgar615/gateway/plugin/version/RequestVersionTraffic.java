package com.github.edgar615.gateway.plugin.version;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.utils.MultimapUtils;

/**
 * Created by Edgar on 2018/4/3.
 *
 * @author Edgar  Date 2018/4/3
 */
public class RequestVersionTraffic implements VersionTraffic {

    private static final String HEADER_NAME = "x-api-version";

    @Override
    public String decision(ApiContext apiContext) {
        String reqVersion = MultimapUtils.getCaseInsensitive(apiContext.headers(), HEADER_NAME);
        return reqVersion;
    }
}
