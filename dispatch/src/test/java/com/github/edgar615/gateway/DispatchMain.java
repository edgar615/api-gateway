package com.github.edgar615.gateway;

import com.github.edgar615.gateway.verticle.ApiDispatchVerticle;
import io.vertx.core.Launcher;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
public class DispatchMain {
  public static void main(String[] args) {
    new Launcher().execute("run", ApiDispatchVerticle.class.getName(),
                           "--cluster",
                           "--conf=H:\\dev\\workspace\\gateway\\dispatch\\src\\main\\conf"
                           + "\\config.json");
  }
}
