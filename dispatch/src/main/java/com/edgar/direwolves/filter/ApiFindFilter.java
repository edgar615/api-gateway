package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiDiscovery;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Helper;
import com.edgar.direwolves.core.utils.Log;
import com.edgar.direwolves.metric.ApiMetrics;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 该filter根据请求从API路由注册表中读取到对应的API定义.
 * <p/>
 * 该filter可以接受下列的配置参数
 * <pre>
 * </pre>
 * <p/>
 * <b>该filter应该在所有的filter之前执行</b>如果未找到对应的API定义，直接返回对应的异常。
 * 该filter的order=-2147483648, int的最小值
 * Created by edgar on 17-1-4.
 */
public class ApiFindFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiFindFilter.class);

  private final Vertx vertx;

  private final ApiDiscovery apiDiscovery;

  public ApiFindFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    String namespace = config.getString("namespace", "");
    this.apiDiscovery = ApiDiscovery.create(vertx, namespace);
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    JsonObject filter = new JsonObject()
            .put("method", apiContext.method().name())
            .put("path", apiContext.path());
    apiDiscovery.getDefinitions(filter, ar -> {
      if (ar.failed()) {
        Log.create(LOGGER)
                .setTraceId(apiContext.id())
                .setEvent("api.discovery.failed")
                .error();
        completeFuture.fail(ar.cause());
        return;
      }
      List<ApiDefinition> apiDefinitions = ar.result();
      if (apiDefinitions.size() != 1) {
        Log.create(LOGGER)
                .setTraceId(apiContext.id())
                .setEvent("api.discovery.failed")
                .error();
        SystemException se = SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND);
        se.set("details", apiContext.method().name() + " " + apiContext.path());
        completeFuture.fail(se);
        return;
      }
      ApiDefinition apiDefinition = apiDefinitions.get(0);
      apiContext.setApiDefinition(apiDefinition);

      try {
        ApiMetrics.instance().request(apiContext.id(), apiDefinition.name());
      } catch (Exception e) {
        e.printStackTrace();
      }
      completeFuture.complete(apiContext);
    });
  }

}
