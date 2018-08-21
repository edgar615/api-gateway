package com.github.edgar615.servicediscovery.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 直接从配置文件中读取服务节点.
 *
 * @author Edgar  Date 2017/6/9
 */
public class JsonServiceDiscoveryVerticle extends AbstractVerticle {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(JsonServiceDiscoveryVerticle.class);

    @Override
    public void start() throws Exception {
        LOGGER.info("[Verticle] [start] start {}",
                    JsonServiceDiscoveryVerticle.class.getSimpleName());
        ServiceDiscoveryOptions options;
        if (config().getValue("service.discovery") instanceof JsonObject) {
            options = new ServiceDiscoveryOptions(config().getJsonObject("service.discovery"));
        } else {
            options = new ServiceDiscoveryOptions();
        }
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx, options);

        if (config().getValue("services") instanceof JsonObject) {
            JsonObject services = config().getJsonObject("services");
            for (String name : services.fieldNames()) {
                JsonArray jsonArray = services.getJsonArray(name);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject service = jsonArray.getJsonObject(i);
                    publishRecord(discovery, name, service);
                }
            }
        }

    }

    private void publishRecord(ServiceDiscovery discovery, String name, JsonObject service) {
        String host = service.getString("host");
        Integer port = service.getInteger("port");
        if (host != null && port != null) {
            Record record = HttpEndpoint.createRecord(name, host, port, "/");
            record.setLocation(service);
            discovery.publish(record, ar -> {
                if (ar.succeeded()) {
                    LOGGER.info("[ServiceDiscovery] [publish] {}", ar.result().toJson().encode());
                } else {
                    LOGGER.error("[ServiceDiscovery] [publish] {}", ar.result().toJson().encode());
                }
            });
        }
    }
}
