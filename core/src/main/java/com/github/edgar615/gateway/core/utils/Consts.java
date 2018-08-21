package com.github.edgar615.gateway.core.utils;

/**
 * 常量.
 *
 * @author Edgar  Date 2018/1/11
 */
public class Consts {
    public static final String DEFAULT_NAMESPACE = "api-gateway";

    public static final int DEFAULT_PORT = 9000;

    public static final String RESPONSE_HEADER = "resp.header:";

    private Consts() {
        throw new AssertionError("Not instantiable: " + Consts.class);
    }
}
