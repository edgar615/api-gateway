package com.edgar.direwolves.web;

import io.vertx.core.Launcher;

public class ServerEample {
  public static void main(String[] args) {
//    System.setProperty("vertx.disableFileCaching", "false");
    new Launcher().execute("run", WebVerticle.class.getName());
  }
}