package com.edgar.direwolves;

import io.vertx.core.Launcher;

/**
 * Created by edgar on 16-9-15.
 */
public class AddAuthCliMain {

  public static void main(String[] args) {
//        CLI cli = CLI.create(AddAuthCliCommand.class);
//        StringBuilder builder = new StringBuilder();
//        cli.usage(builder);
//        System.out.println(builder);

    new Launcher().execute("add-auth", "--name=vert.x", "-t=jwt");
  }
}
