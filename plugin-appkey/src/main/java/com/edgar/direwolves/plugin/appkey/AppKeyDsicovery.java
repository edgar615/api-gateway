package com.edgar.direwolves.plugin.appkey;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/7/20.
 *
 * @author Edgar  Date 2017/7/20
 */
public interface AppKeyDsicovery extends AppKeyPublisher {
  AppKeyDsicovery registerImporter(AppKeyImporter importer, JsonObject config,
                                   Handler<AsyncResult<Void>> completionHandler);

  void close();
}
