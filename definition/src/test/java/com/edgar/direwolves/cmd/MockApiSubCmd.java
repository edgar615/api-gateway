package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiSubCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/1/21.
 *
 * @author Edgar  Date 2017/1/21
 */
public class MockApiSubCmd implements ApiSubCmd {
  @Override
  public String cmd() {
    return "mock.subcmd";
  }

  @Override
  public void handle(ApiDefinition definition, JsonObject jsonObject) {
  }
}
