package com.github.edgar615.gateway.metric;

import com.github.edgar615.gateway.core.cmd.ApiCmd;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;

/**
 * Created by Edgar on 2017/4/1.
 *
 * @author Edgar  Date 2017/4/1
 */
class ApiMetricCmd implements ApiCmd {

    private final MetricsService metricsService;

    ApiMetricCmd(Vertx vertx) {
        this.metricsService = MetricsService.create(vertx);
    }

    @Override
    public String cmd() {
        return "api.metric";
    }

    @Override
    public Future<JsonObject> doHandle(JsonObject jsonObject) {
        try {
            String name = jsonObject.getString("name", "");
            JsonObject metrics = metricsService.getMetricsSnapshot(name);
            if (metrics == null) {
                return Future.succeededFuture(new JsonObject());
            }
            return Future.succeededFuture(metrics);
        } catch (Exception e) {
            return Future.failedFuture(e.getCause());
        }
    }
}
