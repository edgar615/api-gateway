package com.github.edgar615.direwolves.cmd;

import com.github.edgar615.direwolves.core.cmd.ApiCmd;
import com.github.edgar615.direwolves.core.cmd.ApiCmdFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * DeleteApiCmd的工厂类.
 *
 * @author Edgar  Date 2017/1/19
 */
public class DeleteApiCmdFactory implements ApiCmdFactory {
  @Override
  public ApiCmd create(Vertx vertx, JsonObject config) {
    return new DeleteApiCmd(vertx, config);
  }
}
