package com.edgar.direwolves.core.definition;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/1/3.
 *
 * @author Edgar  Date 2017/1/3
 */
@ProxyGen
public interface ApiProvider {

  void match(String method, String path, Handler<AsyncResult<JsonObject>> handler);

}
