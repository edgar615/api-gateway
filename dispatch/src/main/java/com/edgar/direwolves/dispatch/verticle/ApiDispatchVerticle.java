package com.edgar.direwolves.dispatch.verticle;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.dispatch.handler.BaseHandler;
import com.edgar.direwolves.dispatch.handler.DispatchHandler;
import com.edgar.direwolves.dispatch.handler.FailureHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Edgar on 2016/9/12.
 *
 * @author Edgar  Date 2016/9/12
 */
public class ApiDispatchVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiDispatchVerticle.class);

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    LOGGER.info("                _  _                             _                  \n"
                       + "               | |(_)                           | |                 \n"
                       + "             __| | _  _ __  ___ __      __ ___  | |__   __ ___  ___ \n"
                       + "            / _` || || '__|/ _ \\\\ \\ /\\ / // _ \\ | |\\ \\ / // _ "
                       + "\\/ __|\n"
                       + "           | (_| || || |  |  __/ \\ V  V /| (_) || | \\ V /|  __/\\__ "
                       + "\\\n"
                       + "            \\__,_||_||_|   \\___|  \\_/\\_/  \\___/ |_|  \\_/  "
                       + "\\___||___/");

    RedisProvider redisProvider = RedisProvider.create(vertx, config());
    ProxyHelper.registerService(RedisProvider.class, vertx, redisProvider,
                                config().getString("service.cache.address", "cache"));

    DispatchHandler dispatchHandler = DispatchHandler.create(vertx, config());

    int port = config().getInteger("http.port", 8080);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.route().handler(BaseHandler.create());
    router.route().handler(LoggerHandler.create(true, LoggerFormat.DEFAULT));
    router.route().handler(ResponseTimeHandler.create());

    //API拦截
    router.route().handler(dispatchHandler)
            .failureHandler(FailureHandler.create());

    vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(port, ar -> {
              if (ar.succeeded()) {
                LOGGER.info("http server start succeeded, port->{}", port);
                startFuture.complete();
              } else {
                LOGGER.error("http server start failed, port->{}", port, ar.cause());
                startFuture.fail(ar.cause());
              }
            });
  }
}
