package com.github.edgar615.gateway.core.definition;

import com.github.edgar615.util.vertx.spi.JsonObjectCodec;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public interface ApiPluginFactory extends JsonObjectCodec<ApiPlugin> {
    /**
     * @return 插件名称
     */
    String name();

    ApiPlugin create();
}
