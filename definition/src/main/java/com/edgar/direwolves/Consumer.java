package com.edgar.direwolves;

import com.edgar.direwolves.core.spi.SomeService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;

/**
 * Created by edgar on 16-11-30.
 */
public class Consumer extends AbstractVerticle {

  public static void main(String[] args) {
    new Launcher().execute("run", Consumer.class.getName(), "--cluster");
  }

  @Override
  public void start() throws Exception {
    SomeService someService = SomeService.createProxy(vertx);
    someService.process("some-data", ar -> {
      if (ar.succeeded()) {
        System.out.println(ar.result());
      } else {
        ar.cause().printStackTrace();
      }
    });
  }

}
