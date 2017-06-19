package com.edgar.direwolves.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.shell.ShellService;
import io.vertx.ext.shell.ShellServiceOptions;
import io.vertx.ext.shell.term.HttpTermOptions;

/**
 * Created by Edgar on 2017/6/19.
 *
 * @author Edgar  Date 2017/6/19
 */
public class ShellVertice extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    int port = config().getInteger("http.port", 4000);
    ShellService service = ShellService.create(vertx,
                                               new ShellServiceOptions().setHttpOptions(
                                                       new HttpTermOptions().
//                                                               setHost("localhost").
                                                               setPort(port)
                                               ));
    service.start();
  }
}
