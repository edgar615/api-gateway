package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.definition.ApiDefinition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
public interface AppKeyBackend {
  void store(AppKey appKey, Handler<AsyncResult<AppKey>> resultHandler);

  void remove(String appkey, Handler<AsyncResult<AppKey>> resultHandler);

  void getAppKeys(Handler<AsyncResult<List<AppKey>>> resultHandler);

  void getAppKey(String appkey, Handler<AsyncResult<AppKey>> resultHandler);
}
