package com.edgar.direwolves.plugin.ip;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.cmd.ApiCmdFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 *AddBlacklistCmd的工厂类.
 *
 * @author Edgar  Date 2017/1/19
 */
public class AddBlackListCmdFactory implements ApiCmdFactory {
  @Override
  public ApiCmd create(Vertx vertx, JsonObject config) {
    return new AddBlacklistCmd(vertx, config);
  }
}
