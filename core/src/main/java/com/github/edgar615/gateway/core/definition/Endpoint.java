package com.github.edgar615.gateway.core.definition;

/**
 * 定义远程服务调用的格式.
 *
 * @author Edgar  Date 2016/9/12
 */
public interface Endpoint {

    /**
     * @return endpoint的名称
     */
    String name();

    /**
     * @return endpoint的类型
     */
    String type();

}
