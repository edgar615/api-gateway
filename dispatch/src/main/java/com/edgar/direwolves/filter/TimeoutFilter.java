package com.edgar.direwolves.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Helper;
import com.edgar.direwolves.core.utils.MultimapUtils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * 客户端调用时间的校验.
 * 如果开启了这个过滤器，那么对API的调用必须包含下列参数.
 * <pre>
 *   timestamp	时间戳	int	是	unix时间戳
 * </pre>
 * 如果参数不全或者客户端时间与服务端时间相差太久，服务端会认为是非法请求，返回1023的错误。
 * <p>
 * 该filter可以接受下列的配置参数
 * <pre>
 *   timeout.enable 是否启用filter，默认值true
 *   timeout.expires 系统允许客户端或服务端之间的时间误差，单位秒，默认值300
 * </pre>
 * <p>
 * 该filter的order=0
 * Created by edgar on 16-9-20.
 */
public class TimeoutFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutFilter.class);

  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private final int timeout;

  private final boolean enabled;

  TimeoutFilter(JsonObject config) {
    this.timeout = config.getInteger("timeout.expires", 5 * 60);
    commonParamRule.put("timestamp", Rule.required());

    this.enabled = config.getBoolean("timeout.enable", true);
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
    return enabled;
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
      Helper.logFailed(LOGGER, apiContext.id(),
                       this.getClass().getSimpleName(),
                       "timestamp incorrect, client:" + timestamp + ", server:" + currentTime);
      completeFuture.fail(SystemException.create(DefaultErrorCode.EXPIRE)
                                  .set("details", "timestamp:" + timestamp + " is incorrect"));
    } else {
      completeFuture.complete(apiContext);
    }
  }

}
