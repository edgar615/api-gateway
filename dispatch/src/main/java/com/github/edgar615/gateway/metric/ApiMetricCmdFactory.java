package com.github.edgar615.gateway.metric;

import com.github.edgar615.gateway.core.cmd.ApiCmd;
import com.github.edgar615.gateway.core.cmd.ApiCmdFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/4/1.
 *
 * @author Edgar  Date 2017/4/1
 */
public class ApiMetricCmdFactory implements ApiCmdFactory {
  @Override
  public ApiCmd create(Vertx vertx, JsonObject config) {
    return new ApiMetricCmd(vertx);
  }
}
