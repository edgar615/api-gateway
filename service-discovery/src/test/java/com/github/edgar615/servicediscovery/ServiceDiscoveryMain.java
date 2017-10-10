package com.github.edgar615.servicediscovery;

import com.github.edgar615.servicediscovery.verticle.ServiceDiscoveryVerticle;
import io.vertx.core.Launcher;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
public class ServiceDiscoveryMain {
  public static void main(String[] args) {
    new Launcher().execute("run", ServiceDiscoveryVerticle.class.getName(),
                           "--cluster",
                           "--conf=H:\\dev\\workspace\\direwolves\\service-discovery\\src\\test"
                           + "\\conf"
                           + "\\service.json");
  }
}
