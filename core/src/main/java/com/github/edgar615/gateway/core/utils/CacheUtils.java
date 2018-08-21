package com.github.edgar615.gateway.core.utils;

import com.github.edgar615.gateway.core.cache.CacheFactory;
import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheOptions;
import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/11/6.
 *
 * @author Edgar  Date 2017/11/6
 */
public class CacheUtils {
    private static CacheFactory factory = ServiceHelper.loadFactory(CacheFactory.class);

    private CacheUtils() {
        throw new AssertionError("Not instantiable: " + CacheUtils.class);
    }

    /**
     * 创建一个cache的工具类
     * cache
     *
     * @param vertx
     * @param cacheName cache的名称
     * @param options   配置
     * @return
     */
    public static Cache<String, JsonObject> createCache(Vertx vertx,
                                                        String cacheName,
                                                        CacheOptions options) {
        //cache
        return factory.create(vertx, cacheName, options);
    }

}
