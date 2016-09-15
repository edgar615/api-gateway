package com.edgar.direwolves;

import com.edgar.direwolves.cli.AddAuthCliCommand;
import io.vertx.core.Launcher;
import io.vertx.core.cli.CLI;

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
