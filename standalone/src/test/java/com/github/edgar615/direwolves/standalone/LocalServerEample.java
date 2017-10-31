package com.github.edgar615.direwolves.standalone;

import com.github.edgar615.util.vertx.deployment.MainVerticle;
import io.vertx.core.Launcher;

public class LocalServerEample {
  public static void main(String[] args) {
    new Launcher().execute("run", MainVerticle.class.getName(),
                           "--conf=h:\\dev\\workspace\\direwolves\\standalone\\src\\main\\conf"
                           + "\\local-config.json");
  }
}