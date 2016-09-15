package com.edgar.direwolves.cli;

import io.vertx.core.spi.launcher.DefaultCommandFactory;

/**
 * Created by edgar on 16-9-15.
 */
public class AddAuthCliCommandFactory extends DefaultCommandFactory<AddAuthCliCommand> {
    public AddAuthCliCommandFactory() {
        super(AddAuthCliCommand.class);
    }
}
