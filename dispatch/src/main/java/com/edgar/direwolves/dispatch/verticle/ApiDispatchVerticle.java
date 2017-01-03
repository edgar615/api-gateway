package com.edgar.direwolves.dispatch.verticle;

import com.edgar.direwolves.core.cache.CacheProvider;
import com.edgar.direwolves.dispatch.handler.BaseHandler;
import com.edgar.direwolves.dispatch.handler.DispatchHandler;
import com.edgar.direwolves.dispatch.handler.FailureHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
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

    CacheProvider cacheProvider = CacheProvider.create(vertx, config());
    ProxyHelper.registerService(CacheProvider.class, vertx, cacheProvider,
                                config().getString("service.cache.address", "cache"));

    DispatchHandler dispatchHandler = DispatchHandler.create(vertx, config());

    int port = config().getInteger("http.port", 8080);

    Router router = Router.router(vertx);
    Route route = router.route();
    route.handler(BodyHandler.create());

    route.handler(BaseHandler.create());

    //API拦截
    route.handler(dispatchHandler)
            .failureHandler(FailureHandler.create());

    vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(port, ar -> {
              if (ar.succeeded()) {
                startFuture.complete();
              } else {
                startFuture.fail(ar.cause());
              }
            });
  }
}
