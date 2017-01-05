package com.edgar.direwolves.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.MultimapUtils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

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

  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private int timeout = 5 * 60;

  TimeoutFilter(JsonObject config) {
    this.timeout = config.getInteger("timeout.expires", timeout);
    commonParamRule.put("timestamp", Rule.required());
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 0;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    //校验参数
    Validations.validate(apiContext.params(), commonParamRule);
    Multimap<String, String> params = ArrayListMultimap.create(apiContext.params());
    //检查时间戳
    Integer timestamp = Integer.parseInt(MultimapUtils.getFirst(params, "timestamp").toString());
    long currentTime = Instant.now().getEpochSecond();
    if ((timestamp > currentTime + timeout)
        || (timestamp < currentTime - timeout)) {
      completeFuture.fail(SystemException.create(DefaultErrorCode.EXPIRE));
    } else {
      completeFuture.complete(apiContext);
    }
  }

}
