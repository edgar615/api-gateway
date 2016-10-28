package com.edgar.direwolves.eb;

import com.edgar.direwolves.core.definition.ApiDefinition;
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
public class ApiAddHandler implements ApiMessageConsumer<JsonObject> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiAddHandler.class);
  public static final String ADDRESS = "eb.api.add";

  @Override
  public void config(Vertx vertx, JsonObject config) {
    EventBus eb = vertx.eventBus();
    eb.consumer(ADDRESS, this::handle);
  }

  @Override
  public void handle(Message<JsonObject> msg) {
    try {
      JsonObject jsonObject = msg.body();
      ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
      if (apiDefinition != null) {
        ApiDefinitionRegistry.create().add(apiDefinition);
      }
      msg.reply(new JsonObject().put("result", "OK"));
      LOGGER.debug("add api, name->{}", apiDefinition.name());
    } catch (Exception e) {
      LOGGER.error("failed add api, error->{}", e.getMessage(), e);
      msg.fail(-1, e.getMessage());
    }
  }

  @Override
  public String address() {
    return ADDRESS;
  }

}
