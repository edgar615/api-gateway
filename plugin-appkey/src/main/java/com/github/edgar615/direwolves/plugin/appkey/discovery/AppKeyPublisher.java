package com.github.edgar615.direwolves.plugin.appkey.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * AppKey的注册和注销.
 *
 * @author Edgar  Date 2017/2/14
 */
@Deprecated
public interface AppKeyPublisher {
  /**
   * 注册一个AppKey.
   *
   * @param appKey    appKey的JSON对象，里面的属性名要符合appKey、appSecret、appCode、permissions等属性
   * @param resultHandler 回调函数.
   */
  void publish(AppKey appKey, Handler<AsyncResult<AppKey>> resultHandler);

  /**
   * 注销一个AppKey.
   *
   * @param appKey        appKey
   * @param resultHandler 回调函数.
   */
  void unpublish(String appKey, Handler<AsyncResult<Void>> resultHandler);

}
