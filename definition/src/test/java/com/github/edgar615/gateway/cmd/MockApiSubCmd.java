package com.github.edgar615.gateway.cmd;

import com.github.edgar615.gateway.core.cmd.ApiSubCmd;
import com.github.edgar615.gateway.core.definition.ApiDefinition;
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
