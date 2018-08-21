package com.github.edgar615.gateway.standalone;

import com.github.edgar615.util.vertx.deployment.MainVerticle;
import io.vertx.core.Launcher;

public class LocalServerEample {
    public static void main(String[] args) {
        new Launcher().execute("run", MainVerticle.class.getName(),
                               "--conf=h:\\dev\\workspace\\gateway\\standalone\\src\\main\\conf"
                               + "\\local-config.json");
    }
}