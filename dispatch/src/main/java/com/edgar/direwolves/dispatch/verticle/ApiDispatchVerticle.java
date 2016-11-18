package com.edgar.direwolves.dispatch.verticle;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.dispatch.handler.BaseHandler;
import com.edgar.direwolves.dispatch.handler.DispatchHandler;
import com.edgar.direwolves.dispatch.handler.FailureHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Created by Edgar on 2016/9/12.
 *
 * @author Edgar  Date 2016/9/12
 */
public class ApiDispatchVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    //初始化Filter
    List<Filter> filterList = Lists.newArrayList(ServiceLoader.load(Filter.class));
    //排序
    Collections.sort(filterList, (o1, o2) -> o1.order() - o2.order());
    filterList.forEach(filter -> {
      filter.config(vertx, new JsonObject());
    });

    int port = config().getInteger("http.port", 8080);

    Router router = Router.router(vertx);
    Route route = router.route();
    route.handler(BodyHandler.create());

    route.handler(BaseHandler.create());

    //API拦截
    route.handler(new DispatchHandler(filterList))
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
