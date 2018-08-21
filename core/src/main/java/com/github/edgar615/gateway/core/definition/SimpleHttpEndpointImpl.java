package com.github.edgar615.gateway.core.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import io.vertx.core.http.HttpMethod;

/**
 * HTTP的远程调用定义.
 *
 * @author Edgar  Date 2016/9/12
 */
class SimpleHttpEndpointImpl implements SimpleHttpEndpoint {

    /**
     * endpoint名称
     */
    private final String name;

    /**
     * 请求方法 GET | POST | DELETE | PUT
     */
    private final HttpMethod method;

    /**
     * 远程rest路径
     * 示例：/tasks
     * 示例：/tasks/$1/abandon，$1表示当前请求上下文中的$1变量
     */
    private final String path;

    /**
     * 下游服务的host
     */
    private final String host;

    /**
     * 下游服务的端口
     */
    private final int port;

    SimpleHttpEndpointImpl(String name, HttpMethod method, String path,
                           int port,
                           String host) {
        Preconditions.checkNotNull(name, "name can not be null");
        Preconditions.checkNotNull(method, "method can not be null");
        Preconditions.checkNotNull(path, "path can not be null");
        Preconditions.checkNotNull(host, "host can not be null");
        Preconditions.checkArgument(port > 0, "port can not less than 0");
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        this.name = name;
        this.method = method;
        this.path = path;
        this.host = host;
        this.port = port;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(SimpleHttpEndpoint.class.getSimpleName())
                .add("name", name)
                .add("host", host)
                .add("port", port)
                .add("path", path)
                .add("method", method.name())
                .toString();
    }

}