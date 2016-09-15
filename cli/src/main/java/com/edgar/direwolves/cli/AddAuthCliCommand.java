package com.edgar.direwolves.cli;

import io.vertx.core.cli.CLIException;
import io.vertx.core.cli.annotations.Name;
import io.vertx.core.cli.annotations.Option;
import io.vertx.core.cli.annotations.Summary;
import io.vertx.core.spi.launcher.DefaultCommand;

@Name("add-auth")
@Summary("Add Api Auth.")
public class AddAuthCliCommand extends DefaultCommand {

    private String name;

    private String type;

    @Option(shortName = "n", longName = "name")
    public void setName(String name) {
        this.name = name;
    }

    @Option(shortName = "t", longName = "type", choices = {"jwt", "oauth", "app_key"})
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void run() throws CLIException {
        System.out.println("add-auth --name=" + name + " --type=" + type);
    }
}