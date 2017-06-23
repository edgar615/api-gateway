package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiProvider;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.google.common.base.Strings;

import com.edgar.direwolves.metric.ApiMetrics;
import com.edgar.direwolves.core.utils.Helper;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private final ApiProvider apiProvider;

  public ApiFindFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    String namespace = config.getString("namespace", "");
    String address = ApiProvider.class.getName();
    if (!Strings.isNullOrEmpty(namespace)) {
      address = namespace + "." + address;
    }
    this.apiProvider = ProxyHelper.createProxy(ApiProvider.class, vertx, address);
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
    apiProvider.match(apiContext.method().name(), apiContext.path(), ar -> {
      if (ar.succeeded()) {
        try {
          ApiDefinition apiDefinition = ApiDefinition.fromJson(ar.result());
          apiContext.setApiDefinition(apiDefinition);

          ApiMetrics.instance().request(apiContext.id(), apiDefinition.name());
          completeFuture.complete(apiContext);
        } catch (Exception e) {
          Helper.logFailed(LOGGER, apiContext.id(),
                           this.getClass().getSimpleName(),
                           "failed match api");
          completeFuture.fail(e);
        }
      } else {
        Helper.logFailed(LOGGER, apiContext.id(),
                         this.getClass().getSimpleName(),
                         "failed match api");
        completeFuture.fail(ar.cause());
      }
    });
  }

}
