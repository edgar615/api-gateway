package com.edgar.direwolves.example;

import io.vertx.core.Launcher;

public class ServerEample {
  public static void main(String[] args) {
    new Launcher().execute("run", ExampleVerticle.class.getName(),
                           "--conf=H:\\dev\\workspace\\direwolves\\example\\src\\main"
                           + "\\conf\\config.json");

  }
}