package com.edgar.direwolves.redis;

import com.edgar.direwolves.core.spi.UserService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-3.
 */
public class UserServiceImpl implements UserService {
  @Override
  public void save(int userId, JsonObject user, Handler<AsyncResult<JsonObject>> resultHandler) {

  }

  @Override
  public void get(int userId, Handler<AsyncResult<JsonObject>> resultHandler) {

  }
}
