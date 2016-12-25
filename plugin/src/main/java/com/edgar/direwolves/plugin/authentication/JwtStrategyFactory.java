package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.auth.AuthenticationStrategyFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/12/18.
 *
 * @author Edgar  Date 2016/12/18
 */
public class JwtStrategyFactory implements AuthenticationStrategyFactory {
  @Override
  public String name() {
    return "jwt";
  }

  @Override
  public JwtStrategy create(Vertx vertx, JsonObject config) {
    return new JwtStrategy(vertx, config);
  }
}
