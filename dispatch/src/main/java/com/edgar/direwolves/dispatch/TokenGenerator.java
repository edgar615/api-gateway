package com.edgar.direwolves.dispatch;

import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-9-20.
 */
public interface TokenGenerator {

    /**
     * 创建token
     *
     * @return
     */
    String createToken(JsonObject claims);
}
