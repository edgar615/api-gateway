package com.github.edgar615.direwolves.verticle;

import com.github.edgar615.direwolves.core.cmd.CmdRegister;
import com.github.edgar615.direwolves.dispatch.BaseHandler;
import com.github.edgar615.direwolves.dispatch.DispatchHandler;
import com.github.edgar615.direwolves.dispatch.FailureHandler;
import com.github.edgar615.util.log.Log;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
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
    Log.create(LOGGER)
            .setEvent("dispatch.deploying")
            .addData("config", config())
            .info();

    String namespace = config().getString("namespace", "api-gateway");

    //读取命令
    Future<Void> importCmdFuture = Future.future();
    new CmdRegister().initialize(vertx, config(), importCmdFuture);

    //Diapatch
    DispatchHandler dispatchHandler = DispatchHandler.create(vertx, config());

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.route().handler(BaseHandler.create());
    router.route().handler(ResponseTimeHandler.create());

    //API拦截
    router.route().handler(dispatchHandler)
            .failureHandler(FailureHandler.create());

    vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(config().getInteger("port", 8080), ar -> {
              if (ar.succeeded()) {
                Log.create(LOGGER)
                        .setEvent("dispatch.start.succeeded")
                        .addData("namespace", namespace)
                        .addData("port", config().getInteger("port", 8080))
                        .info();
                LOGGER.info("---| [Diaptacher Start] [OK] [{}]",
                            config().getInteger("port", 8080));
                startFuture.complete();
              } else {
                Log.create(LOGGER)
                        .setEvent("dispatch.start.failed")
                        .addData("namespace", namespace)
                        .addData("port", config().getInteger("port", 8080))
                        .setThrowable(ar.cause())
                        .error();
                startFuture.fail(ar.cause());
              }
            });
  }
}
