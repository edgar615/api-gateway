package com.edgar.direwolves.dispatch.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * http请求的辅助类.所有的请求都可以接受这个处理类，该类主要做一下通用的设置然后就会将请求传递给下一个处理类.
 * <p>
 * 设置响应的content-type为application/json;charset=utf-8
 *
 * @author Edgar  Date 2016/2/18
 */
public class BaseHandler implements Handler<RoutingContext> {

  public static Handler<RoutingContext> create() {
    return new BaseHandler();
  }

  @Override
  public void handle(RoutingContext rc) {
    rc.response().setChunked(true)
            .putHeader("content-type", "application/json;charset=utf-8");
    rc.next();
  }

}