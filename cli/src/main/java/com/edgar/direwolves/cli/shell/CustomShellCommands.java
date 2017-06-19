package com.edgar.direwolves.cli.shell;

import com.edgar.direwolves.cli.shell.service.ServiceIncrWeightCommand;
import com.edgar.direwolves.cli.shell.service.ServiceDecrWeightCommand;
import com.edgar.direwolves.cli.shell.service.ServiceInfoCommand;
import com.edgar.direwolves.cli.shell.service.ServiceLsCommand;
import com.edgar.direwolves.cli.shell.service.ServiceStatCommand;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.shell.command.CommandRegistry;
import io.vertx.ext.shell.command.CommandResolver;
import io.vertx.ext.shell.spi.CommandResolverFactory;

public class CustomShellCommands implements CommandResolverFactory {

  public void resolver(Vertx vertx, Handler<AsyncResult<CommandResolver>> resolverHandler) {
    CommandRegistry registry = CommandRegistry.getShared(vertx);
    registry.registerCommand(ServiceLsCommand.class);
    registry.registerCommand(ServiceInfoCommand.class);
    registry.registerCommand(ServiceStatCommand.class);
    registry.registerCommand(ServiceIncrWeightCommand.class);
    registry.registerCommand(ServiceDecrWeightCommand.class);
    resolverHandler.handle(Future.succeededFuture(registry));
  }
}