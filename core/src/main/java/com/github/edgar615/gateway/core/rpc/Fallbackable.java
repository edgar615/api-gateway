package com.github.edgar615.gateway.core.rpc;

/**
 * 用于表示降级支持.
 *
 * @author Edgar  Date 2017/8/25
 */
public interface Fallbackable {

    void setFallback(RpcResponse fallback);

    RpcResponse fallback();
}
