package com.edgar.direwolves;

import io.vertx.core.Launcher;
import io.vertx.ext.shell.ShellVerticle;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
public class ShellMain {
  public static void main(String[] args) {
    new Launcher().execute("run", ShellVerticle.class.getName(),
                           "--cluster",
                           "--conf=H:\\dev\\workspace\\direwolves\\cli\\src\\main\\conf"
                           + "\\shell.json");
  }
}
