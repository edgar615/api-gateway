package com.edgar.direwolves.core.spi;

import com.edgar.direwolves.core.auth.AuthenticationStrategy;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public interface Factory<T> {

  /**
   * @return 策略名称
   */
  String name();

  T create(Vertx vertx, JsonObject config);
}
