package com.edgar.direwolves.definition;

import com.edgar.direwolves.core.spi.EventbusMessageConsumer;
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
  public static final String API_ADD = "api.add";

  public final JsonObject RESULT_OK = new JsonObject().put("result", "OK");

  @Override
  public void config(Vertx vertx, JsonObject config) {
    EventBus eb = vertx.eventBus();
    eb.consumer(API_ADD, this::handle);
  }

  @Override
  public void handle(Message<JsonObject> msg) {
    try {
      JsonObject jsonObject = msg.body();
      ApiDefinition apiDefinition = ApiDefinition.fromJson(jsonObject);
      if (apiDefinition != null) {
        ApiDefinitionRegistry.create().add(apiDefinition);
      }

      msg.reply(RESULT_OK);
    } catch (Exception e) {
      msg.fail(-1, e.getMessage());
    }
  }

  @Override
  public String address() {
    return API_ADD;
  }
}
