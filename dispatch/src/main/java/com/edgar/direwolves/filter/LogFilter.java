package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Edgar on 2017/2/15.
 *
 * @author Edgar  Date 2017/2/15
 */
public class LogFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogFilter.class);

  private final Set<String> ignores = new HashSet<>();

  private final long longReqTime;

  LogFilter(JsonObject config) {
    JsonArray jsonArray = config.getJsonArray("log.ignore", new JsonArray());
    for (int i = 0; i < jsonArray.size(); i++) {
      ignores.add(jsonArray.getString(i));
    }
    this.longReqTime = config.getLong("long_req_time", 1000l);
  }

  @Override
  public String type() {
    return AFTER_RESP;
  }

  @Override
  public int order() {
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return true;
    }
    if (ignores.contains(apiContext.apiDefinition().name())) {
      return false;
    }
    return true;

  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    long started = (long) apiContext.variables()
            .getOrDefault("request.time", System.currentTimeMillis());
    long ended = System.currentTimeMillis();
    long duration = ended - started;
    if (duration > longReqTime) {
      LOGGER.warn("Request succeed, id->{},duration->{}, context->{}",
                  apiContext.id(),
                  duration,
                  apiContext);
    } else {
      LOGGER.info("Request succeed, id->{},duration->{}, context->{}",
                  apiContext.id(),
                  duration,
                  apiContext);
    }
  }
}
