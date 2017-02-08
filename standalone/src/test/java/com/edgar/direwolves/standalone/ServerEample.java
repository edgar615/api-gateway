package com.edgar.direwolves.standalone;

import io.vertx.core.Launcher;

public class ServerEample {
  public static void main(String[] args) {
    new Launcher().execute("run", StartupVerticle.class.getName(),
                           "--conf=H:\\dev\\workspace\\direwolves\\standalone\\src\\main\\conf"
                           + "\\config.json");

  }
}