package com.github.edgar615.gateway.standalone;

import com.github.edgar615.util.vertx.deployment.MainVerticle;
import io.vertx.core.Launcher;

public class ServerEample {
  public static void main(String[] args) {
    new Launcher().execute("run", MainVerticle.class.getName(),
                           "--conf=H:\\dev\\workspace\\gateway\\standalone\\src\\main\\conf"
                           + "\\config.json");
//    new Launcher().execute("run", StartupVerticle.class.getName(),
//        "--conf=/home/edgar/dev/workspace/gateway/standalone/src/main/conf"
//            + "/config.json");
  }
}