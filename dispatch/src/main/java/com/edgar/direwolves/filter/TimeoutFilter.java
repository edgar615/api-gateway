package com.edgar.direwolves.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.List;

/**
 * 客户端调用时间的校验.
 * 如果开启了这个过滤器，那么对API的调用必须包含下列参数，如果缺少任意一个，服务端会认为是非法请求。
 * <pre>
 *   timestamp	时间戳	int	是	unix时间戳
 * </pre>
 * 默认允许客户端和服务端有5分钟的误差，可以通过timeout.expires配置项来配置误差范围.
 * Created by edgar on 16-9-20.
 */
public class TimeoutFilter implements Filter {

  private static final String TYPE = "timeout";

  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private Vertx vertx;

  private int timeout = 5 * 60;

  private JsonArray secrets = new JsonArray();

  public TimeoutFilter() {
//        commonParamRule.put("nonce", Rule.required());
    commonParamRule.put("timestamp", Rule.required());
  }

  @Override
  public String type() {
    return TYPE;
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.timeout = config.getInteger("timeout.expires", timeout);
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return false;
    }
    List<String> filters = apiContext.apiDefinition().filters();
    return filters.contains(TYPE);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    //校验参数
    Validations.validate(apiContext.params(), commonParamRule);
    Multimap<String, String> params = ArrayListMultimap.create(apiContext.params());
    //检查时间戳
    Integer timestamp = Integer.parseInt(getFirst(params, "timestamp").toString());
    long currentTime = Instant.now().getEpochSecond();
    if ((timestamp > currentTime + timeout)
        || (timestamp < currentTime - timeout)) {
      completeFuture.fail(SystemException.create(DefaultErrorCode.EXPIRE));
    } else {
      completeFuture.complete(apiContext);
    }
  }

}
