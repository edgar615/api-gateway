package com.edgar.direwolves.core.dispatch;

import com.edgar.direwolves.core.spi.Configurable;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public interface AuthenticationStrategy extends Configurable {

  void doAuthentication(ApiContext apiContext, Future<JsonObject> completeFuture);

  String name();
}
