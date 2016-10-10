package com.edgar.direwolves.definition.eb;

import com.edgar.direwolves.core.spi.EventbusMessageConsumer;
import com.edgar.direwolves.definition.ApiDefinition;
import com.edgar.direwolves.definition.verticle.ApiDefinitionRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public class AddApiHandler implements EventbusMessageConsumer<JsonObject> {
  public static final String ADDRESS = "api.add";

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
    } catch (Exception e) {
      msg.fail(-1, e.getMessage());
    }
  }

  @Override
  public String address() {
    return ADDRESS;
  }
}
