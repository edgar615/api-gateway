package com.github.edgar615.gateway.plugin.transformer;

import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 将endpoint转换为json对象.
 * params和header的json均为{"k1", ["v1"]}，{"k1", ["v1", "v2]}格式的json对象.
 * <p>
 * <pre>
 *   {
 * "id" : "5bbbe06b-df08-4728-b5e2-166faf912621",
 * "type" : "http",
 * "path" : "/devices",
 * "method" : "POST",
 * "params" : {
 * "q3" : [ "v3" ]
 * },
 * "headers" : {
 * "h3" : [ "v3", "v3.2" ]
 * },
 * "body" : {
 * "foo" : "bar"
 * },
 * "host" : "localhost",
 * "port" : 8080
 * }
 * </pre>
 * <p>
 * Created by edgar on 16-9-20.
 */
public class RequestTransformerFilterFactory implements FilterFactory {


    @Override
    public String name() {
        return HttpRequestTransformerFilter.class.getSimpleName();
    }

    @Override
    public Filter create(Vertx vertx, JsonObject config) {
        return new HttpRequestTransformerFilter(config);
    }
}