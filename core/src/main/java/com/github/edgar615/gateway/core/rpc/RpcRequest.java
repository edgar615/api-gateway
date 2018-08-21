package com.github.edgar615.gateway.core.rpc;

public interface RpcRequest {

    /**
     * @return id
     */
    String id();

    /**
     * @return 名称
     */
    String name();

    /**
     * @return 类型
     */
    String type();

    /**
     * 复制RPC请求
     *
     * @return RpcRequest
     */
    RpcRequest copy();

}