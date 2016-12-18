package com.edgar.direwolves.core.auth;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.spi.Configurable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public interface AuthenticationStrategyFactory  {

  /**
   * @return 策略名称
   */
  String name();

  AuthenticationStrategy create(Vertx vertx, JsonObject config);
}
