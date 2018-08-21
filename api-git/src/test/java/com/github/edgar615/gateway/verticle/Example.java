package com.github.edgar615.gateway.verticle;

import io.vertx.core.Launcher;

/**
 * Created by Edgar on 2018/2/1.
 *
 * @author Edgar  Date 2018/2/1
 */
public class Example {
    public static void main(String[] args) {
        new Launcher().execute("run", ApiGitVerticle.class.getName(),
                               "--worker",
                               "--conf=H:\\dev\\workspace\\api-gateway\\api-git\\src\\main"
                               + "\\conf"
                               + "\\api.git.json");
    }
}
