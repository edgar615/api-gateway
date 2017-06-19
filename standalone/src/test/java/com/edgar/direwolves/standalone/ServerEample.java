package com.edgar.direwolves.standalone;

import com.edgar.util.vertx.deployment.MainVerticle;
import io.vertx.core.Launcher;

public class ServerEample {
  public static void main(String[] args) {
    new Launcher().execute("run", MainVerticle.class.getName(),
                           "--conf=H:\\dev\\workspace\\direwolves\\standalone\\src\\main\\conf"
                           + "\\config.json");
//    new Launcher().execute("run", StartupVerticle.class.getName(),
//        "--conf=/home/edgar/dev/workspace/direwolves/standalone/src/main/conf"
//            + "/config.json");
  }
}