package com.edgar.direwolves.eb;

import com.edgar.direwolves.core.spi.EventbusMessageConsumer;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public class ApiDeleteHandler implements ApiMessageConsumer<String> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiDeleteHandler.class);
  public static final String ADDRESS = "api.delete";

  @Override
  public void config(Vertx vertx, JsonObject config) {
    EventBus eb = vertx.eventBus();
    eb.consumer(ADDRESS, this::handle);
  }

  @Override
  public void handle(Message<String> msg) {
    try {
      String name = msg.body();
      ApiDefinitionRegistry.create().remove(name);
      LOGGER.debug("delete api, name->{}", name);
      msg.reply(new JsonObject().put("result", "OK"));
    } catch (Exception e) {
      LOGGER.error("failed delete api, error->{}", e.getMessage(), e);
      msg.fail(-1, e.getMessage());
    }
  }

  @Override
  public String address() {
    return ADDRESS;
  }
}
