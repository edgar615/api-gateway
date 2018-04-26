package com.github.edgar615.direwolves.verticle;

import com.github.edgar615.direwolves.core.cmd.CmdRegister;
import com.github.edgar615.direwolves.core.utils.Consts;
import com.github.edgar615.direwolves.core.utils.Log;
import com.github.edgar615.direwolves.dispatch.BaseHandler;
import com.github.edgar615.direwolves.dispatch.DispatchHandler;
import com.github.edgar615.direwolves.dispatch.FailureHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
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
    LOGGER.info("ApiDispatchVerticle deploying, config:{}", config());

    //读取命令
    Future<Void> importCmdFuture = Future.future();
    new CmdRegister().initialize(vertx, config(), importCmdFuture);

    //Diapatch
    DispatchHandler dispatchHandler = DispatchHandler.create(vertx, config());

    Router router = Router.router(vertx);
    BodyHandler bodyHandler = BodyHandler.create();
    if (config().containsKey("bodyLimit")) {
      bodyHandler.setBodyLimit(config().getLong("bodyLimit"));
    }
    router.route().handler(bodyHandler);
    checkContainsCors(router);

    router.route().handler(BaseHandler.create());
    router.route().handler(ResponseTimeHandler.create());

    //API拦截
    router.route().handler(dispatchHandler)
            .failureHandler(FailureHandler.create());

    startHttpServer(router, startFuture);
  }

  private void startHttpServer(Router router, Future<Void> startFuture) {HttpServerOptions options;
    if (config().getValue("http") instanceof JsonObject) {
      options = new HttpServerOptions(config().getJsonObject("http"));
    } else {
      options = new HttpServerOptions();
    }

    String namespace = config().getString("namespace", Consts.DEFAULT_NAMESPACE);
    int port = config().getInteger("port", Consts.DEFAULT_PORT);
    vertx.createHttpServer(options)
            .requestHandler(router::accept)
            .listen(config().getInteger("port", port), ar -> {
              if (ar.succeeded()) {
                LOGGER.info("ApiDispatchVerticle deploy succeeded, namespace:{}, port", namespace, port);
                startFuture.complete();
              } else {
                LOGGER.error("ApiDispatchVerticle deploy failed, namespace:{}, port", namespace, port, ar.cause());
                startFuture.fail(ar.cause());
              }
            });
  }

  private void checkContainsCors(Router router) {
    if (config().getValue("cors") instanceof JsonObject) {
      JsonObject corsConfig = config().getJsonObject("cors");
      String allowedOriginPattern = corsConfig.getString("allowedOriginPattern", "*");
      CorsHandler corsHandler = CorsHandler.create(allowedOriginPattern);
      if (config().getValue("allowedMethods") instanceof JsonArray) {
        config().getJsonArray("allowedMethods").forEach(o -> {
          if (o instanceof String) {
            String method = (String) o;
            corsHandler.allowedMethod(HttpMethod.valueOf(method.toUpperCase()));
          }
        });
      }
      if (config().getValue("allowedHeaders") instanceof JsonArray) {
        config().getJsonArray("allowedHeaders").forEach(o -> {
          if (o instanceof String) {
            String headerName = (String) o;
            corsHandler.allowedHeader(headerName);
          }
        });
      }
      if (config().getValue("allowCredentials") instanceof Boolean) {
        corsHandler.allowCredentials(config().getBoolean("allowCredentials"));
      }
      if (config().getValue("maxAgeSeconds") instanceof Integer) {
        corsHandler.maxAgeSeconds(config().getInteger("maxAgeSeconds"));
      }
    router.route().handler(corsHandler);
    }
  }
}
