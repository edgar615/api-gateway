package com.github.edgar615.gateway.plugin.fallback;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.rpc.RpcResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * 降级的插件.
 * 该插件对应的JSON配置的key为<b>request.fallback</b>，它的值是一个json对象，key对应着endpoint的名称，value也是一个json对象，定义了降级的结果:
 * <pre>
 *   name 参数名称
 *   default_value 参数的默认值，如果请求的参数为null，使用默认值代替，默认值null
 *   rules 校验规则，数组，详细格式参考RulesDecoder
 * </pre>
 * json配置:
 * <pre>
 *   "request.fallback": {
 * "add_device": {
 * "statusCode" : 200,
 * "result" : {
 * "foo": "bar"
 * }
 * },
 * "device.list" : {
 * "statusCode" : 200,
 * "result" : []
 * }
 * }
 *
 * </pre>
 *
 * @author Edgar  Date 2017/7/6
 */
public class FallbackPlugin implements ApiPlugin {

    private final Map<String, RpcResponse> fallback = new HashMap<>();

    @Override
    public String name() {
        return FallbackPlugin.class.getSimpleName();
    }

    public FallbackPlugin addFallBack(String name, RpcResponse rpcResponse) {
        this.fallback.put(name, rpcResponse);
        return this;
    }

    public Map<String, RpcResponse> fallback() {
        return fallback;
    }
}
