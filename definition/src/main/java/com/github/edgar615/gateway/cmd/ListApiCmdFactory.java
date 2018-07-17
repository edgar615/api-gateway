package com.github.edgar615.gateway.cmd;

import com.github.edgar615.gateway.core.cmd.ApiCmd;
import com.github.edgar615.gateway.core.cmd.ApiCmdFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * ListApiCmd的工厂类.
 *
 * @author Edgar  Date 2017/1/19
 */
public class ListApiCmdFactory implements ApiCmdFactory {
  @Override
  public ApiCmd create(Vertx vertx, JsonObject config) {
    return new ListApiCmd(vertx, config);
  }
}
