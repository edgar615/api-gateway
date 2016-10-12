package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.filter.Filters;
import com.edgar.direwolves.service.RecordSelect;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Created by Edgar on 2016/9/12.
 *
 * @author Edgar  Date 2016/9/12
 */
public class DispatchVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    //初始化Filter
    Filters filters = Filters.instance();
    filters.init(vertx);

    //服务发现
    RecordSelect recordSelect = RecordSelect.create();
    recordSelect.config(vertx, config());

    int port = config().getInteger("http.port", 8080);

    Router router = Router.router(vertx);
    Route route = router.route();
    route.handler(BodyHandler.create());

    route.handler(BaseHandler.create());

    //API拦截
    route.handler(new DispatchHandler())
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
