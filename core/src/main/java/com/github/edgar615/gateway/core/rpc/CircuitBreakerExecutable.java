package com.github.edgar615.gateway.core.rpc;

/**
 * 用于表示断路器支持.
 *
 * @author Edgar  Date 2017/8/25
 */
public interface CircuitBreakerExecutable {

    /**
     * 断路器名称
     *
     * @return
     */
    String circuitBreakerName();
}
