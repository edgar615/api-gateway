package com.edgar.direwolves.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;

import java.io.File;

public class WebVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {

    HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create();

    Router router = Router.router(vertx);
    router.get("/clear").handler(rc -> {
      engine.clearCache();
//      System.out.println(new File(".vertx").getAbsolutePath());
//      rc.vertx().fileSystem().deleteRecursive(new File(".vertx").getAbsolutePath(), true, ar -> {
//        System.out.println(ar.result());
//      });
      rc.response().setStatusCode(200).end("clear");
    });

    router.get().handler(rc -> {
      rc.put("name", "vert.x webhoho");

      engine.render(rc, "templates/index.hbs", res -> {
        if (res.succeeded()) {
          rc.response().end(res.result());
        } else {
          rc.fail(res.cause());
        }
      });
    });
    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}