package com.edgar.direwolves.core.auth;

import com.edgar.direwolves.core.dispatch.ApiContext;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public interface AuthenticationStrategy {

  void doAuthentication(ApiContext apiContext, Future<JsonObject> completeFuture);

  String name();

}
